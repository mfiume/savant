/*
 *    Copyright 2010 University of Toronto
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */

package savant.data.types;

import java.awt.Color;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Serializable;
import java.net.URI;
import java.util.*;
import javax.xml.parsers.DocumentBuilderFactory;
import org.w3c.dom.Document;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import savant.api.adapter.GenomeAdapter;
import savant.api.adapter.RangeAdapter;
import savant.controller.ReferenceController;
import savant.data.sources.file.FASTAFileDataSource;
import savant.settings.BrowserSettings;
import savant.util.NetworkUtils;
import savant.util.Resolution;
import savant.view.swing.Track;
import savant.view.swing.sequence.SequenceTrack;

/**
 *
 * @author mfiume, vwilliams
 */
public final class Genome implements Serializable, GenomeAdapter {
    private static final Log LOG = LogFactory.getLog(Genome.class);

    private final String name;
    private final String description;
    private Map<String, Integer> referenceMap = new HashMap<String, Integer>();
    private Map<String, Cytoband[]> cytobands;
    private URI cytobandURI;
    private Gene[] genes;

    // if associated with track
    private SequenceTrack track;
    private FASTAFileDataSource dataSource;

    public Genome(String name, SequenceTrack track) {
        this.name = name;
        this.track = track;
        this.description = null;
        dataSource = (FASTAFileDataSource)track.getDataSource();

        for (String refname : dataSource.getReferenceNames()) {
            referenceMap.put(refname, dataSource.getLength(refname));
        }
    }

    /**
     * Populate a genome using only an array containing chromsome names and lengths.
     */
    private Genome(String name, String desc, ReferenceInfo[] info) {
        this.name = name;
        this.description = desc;
        for (ReferenceInfo i: info) {
            referenceMap.put(i.name, i.length);
        }
    }

    /**
     * Create a Genome from one of the .cytoband files on our website.  Note that
     * this does a lazy instantiation of the reference map.
     *
     * @param uri
     */
    public Genome(String name, String desc, URI cytobandURI, Gene[] genes) throws IOException {
        this.name = name;
        this.description = desc;
        this.cytobandURI = cytobandURI;
        this.genes = genes;
    }

    public Genome(String name, int length) {
        this.name = "user defined";
        this.description = null;
        referenceMap.put(name, length);
    }

    @Override
    public Set<String> getReferenceNames() {
        return getReferenceMap().keySet();
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public byte[] getSequence(String reference, RangeAdapter range) throws IOException {
        return isSequenceSet() ? dataSource.getRecords(reference, range, Resolution.VERY_HIGH).get(0).getSequence() : null;
    }

    @Override
    public int getLength() {
        return getLength(ReferenceController.getInstance().getReferenceName());
    }

    @Override
    public int getLength(String reference) {
        return getReferenceMap().get(reference);
    }

    @Override
    public FASTAFileDataSource getDataSource() {
        return dataSource;
    }

    @Override
    public boolean isSequenceSet() {
        return dataSource != null;
    }

    @Override
    public Track getTrack() {
        return track;
    }

    @Override
    public String toString() {
        return description != null ? description : name;
    }

    public int getReferenceLength(String refname) {
        return getReferenceMap().containsKey(refname) ? referenceMap.get(refname) : -1;
    }

    private Map<String, Integer> getReferenceMap() {
        if (referenceMap.isEmpty() && cytobandURI != null) {
            loadCytobands();
        }
        return referenceMap;
    }

    public Cytoband[] getCytobands(String ref) {
        if (cytobands == null && cytobandURI != null) {
            loadCytobands();
        }
        return cytobands != null ? cytobands.get(ref) : null;
    }

    private void loadCytobands() {
        cytobands = new LinkedHashMap<String, Cytoband[]>();

        try {
            BufferedReader reader = new BufferedReader(new InputStreamReader(NetworkUtils.getSeekableStreamForURI(cytobandURI)));
            String line = null;
            String chrom = null;
            List<Cytoband> chromBands = new ArrayList<Cytoband>();
            while ((line = reader.readLine()) != null) {
                String[] tokens = line.split("\t");
                if (!tokens[0].equals(chrom)) {
                    addCytobands(chrom, chromBands);
                    chrom = tokens[0];
                }
                chromBands.add(new Cytoband(tokens));
            }
            addCytobands(chrom, chromBands);
        } catch (IOException iox) {
            LOG.error("Unable to load cytoband info from " + cytobandURI, iox);
        }
    }

    /**
     * Add a list of cytobands for the given chromosome.  Also calculates the length of the chromosome.
     *
     * @param chrom the chromosome for which bands are being set
     * @param bands a list of bands
     */
    private void addCytobands(String chrom, List<Cytoband> bands) {
        if (chrom != null) {
            referenceMap.put(chrom, bands.get(bands.size() - 1).end - bands.get(0).start);
            cytobands.put(chrom, bands.toArray(new Cytoband[0]));
        }
        bands.clear();
    }

    /**
     * Try to load the default genomes from our web-site.  If that fails, return an array containing 5 popular genomes.
     * @return
     */
    public static Genome[] getDefaultGenomes() {
        try {
            List<Genome> result = new ArrayList<Genome>();
            Document doc = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(BrowserSettings.GENOMES_URL);
            NodeList genomes = doc.getElementsByTagName("genome");
            for (int i = 0; i < genomes.getLength(); i++) {
                Element genome = (Element)genomes.item(i);
                Element cytoband = (Element)genome.getElementsByTagName("cytoband").item(0);
                NodeList geneNodes = genome.getElementsByTagName("gene");
                Gene[] genes = new Gene[geneNodes.getLength()];
                for (int j = 0; j < genes.length; j++) {
                    Element geneElement = (Element)geneNodes.item(j);
                    genes[j] = new Gene(geneElement.getAttribute("description"), URI.create(geneElement.getAttribute("uri")));
                }
                result.add(new Genome(genome.getAttribute("name"), genome.getAttribute("description"), URI.create(cytoband.getAttribute("uri")), genes));
            }
            return result.toArray(new Genome[0]);
        } catch (Exception x) {
            LOG.error("Unable to load default genomes from " + BrowserSettings.DATA_URL + "; using built-ins.", x);

            return new Genome[] {
                new Genome("hg19", "Human - Feb. 2009 (GRCh37/hg19)", new ReferenceInfo[] {
                    new ReferenceInfo("chr1", 249250621), new ReferenceInfo("chr2", 243199373), new ReferenceInfo("chr3", 198022430),
                    new ReferenceInfo("chr4", 191154276), new ReferenceInfo("chr5", 180915260), new ReferenceInfo("chr6", 171115067),
                    new ReferenceInfo("chr7", 159138663), new ReferenceInfo("chr8", 146364022), new ReferenceInfo("chr9", 141213431),
                    new ReferenceInfo("chr10", 135534747), new ReferenceInfo("chr11", 135006516), new ReferenceInfo("chr12", 133851895),
                    new ReferenceInfo("chr13", 115169878), new ReferenceInfo("chr14", 107349540), new ReferenceInfo("chr15", 102531392),
                    new ReferenceInfo("chr16", 90354753), new ReferenceInfo("chr17", 81195210), new ReferenceInfo("chr18", 78077248),
                    new ReferenceInfo("chr19", 59128983), new ReferenceInfo("chr20", 63025520), new ReferenceInfo("chr21", 48129895),
                    new ReferenceInfo("chr22", 51304566), new ReferenceInfo("chrX", 155270560), new ReferenceInfo("chrY", 59373566) }),
                new Genome("hg18", "Human - Mar. 2006 (NCBI36/hg18)", new ReferenceInfo[] {
                    new ReferenceInfo("chr1", 247249719), new ReferenceInfo("chr2", 242951149), new ReferenceInfo("chr3", 199501827),
                    new ReferenceInfo("chr4", 191273063), new ReferenceInfo("chr5", 180857866), new ReferenceInfo("chr6", 170899992),
                    new ReferenceInfo("chr7", 158821424), new ReferenceInfo("chr8", 146274826), new ReferenceInfo("chr9", 140273252),
                    new ReferenceInfo("chr10", 135374737), new ReferenceInfo("chr11", 134452384), new ReferenceInfo("chr12", 132349534),
                    new ReferenceInfo("chr13", 114142980), new ReferenceInfo("chr14", 106368585), new ReferenceInfo("chr15", 100338915),
                    new ReferenceInfo("chr16", 88827254), new ReferenceInfo("chr17", 78774742), new ReferenceInfo("chr18", 76117153),
                    new ReferenceInfo("chr19", 63811651), new ReferenceInfo("chr20", 62435964), new ReferenceInfo("chr21", 46944323),
                    new ReferenceInfo("chr22", 49691432), new ReferenceInfo("chrX", 154913754), new ReferenceInfo("chrY", 57772954) }),
                new Genome("hg17", "Human - May 2004 (NCBI35/hg17)", new ReferenceInfo[] {
                    new ReferenceInfo("chr1", 245442847), new ReferenceInfo("chr2", 242818229), new ReferenceInfo("chr3", 199450740),
                    new ReferenceInfo("chr4", 191401218), new ReferenceInfo("chr5", 180837866), new ReferenceInfo("chr6", 170972699),
                    new ReferenceInfo("chr7", 158628139), new ReferenceInfo("chr8", 146274826), new ReferenceInfo("chr9", 138429268),
                    new ReferenceInfo("chr10", 135413628), new ReferenceInfo("chr11", 134452384), new ReferenceInfo("chr12", 132389811),
                    new ReferenceInfo("chr13", 114127980), new ReferenceInfo("chr14", 106360585), new ReferenceInfo("chr15", 100338915),
                    new ReferenceInfo("chr16", 88822254), new ReferenceInfo("chr17", 78654742), new ReferenceInfo("chr18", 76117153),
                    new ReferenceInfo("chr19", 63806651), new ReferenceInfo("chr20", 62435964), new ReferenceInfo("chr21", 46944323),
                    new ReferenceInfo("chr22", 49534710), new ReferenceInfo("chrX", 154824264), new ReferenceInfo("chrY", 57701691) }),
                new Genome("mm9", "Mouse - July 2007 (NCBI37/mm9)", new ReferenceInfo[] {
                    new ReferenceInfo("chr1", 197195432), new ReferenceInfo("chr2", 181748087), new ReferenceInfo("chr3", 159599783),
                    new ReferenceInfo("chr4", 155630120), new ReferenceInfo("chr5", 152537259), new ReferenceInfo("chr6", 149517037),
                    new ReferenceInfo("chr7", 152524553), new ReferenceInfo("chr8", 131738871), new ReferenceInfo("chr9", 124076172),
                    new ReferenceInfo("chr10", 129993255), new ReferenceInfo("chr11", 121843856), new ReferenceInfo("chr12", 121257530),
                    new ReferenceInfo("chr13", 120284312), new ReferenceInfo("chr14", 125194864), new ReferenceInfo("chr15", 103494974),
                    new ReferenceInfo("chr16", 98319150), new ReferenceInfo("chr17", 95272651), new ReferenceInfo("chr18", 90772031),
                    new ReferenceInfo("chr19", 61342430), new ReferenceInfo("chrX", 166650296), new ReferenceInfo("chrY", 15902555) }),
                new Genome("mm8", "Mouse - Feb. 2006 (NCBI36/mm8)", new ReferenceInfo[] {
                    new ReferenceInfo("chr1", 197069962), new ReferenceInfo("chr2", 181976762), new ReferenceInfo("chr3", 159872112),
                    new ReferenceInfo("chr4", 155029701), new ReferenceInfo("chr5", 152003063), new ReferenceInfo("chr6", 149525685),
                    new ReferenceInfo("chr7", 145134094), new ReferenceInfo("chr8", 132085098), new ReferenceInfo("chr9", 124000669),
                    new ReferenceInfo("chr10", 129959148), new ReferenceInfo("chr11", 121798632), new ReferenceInfo("chr12", 120463159),
                    new ReferenceInfo("chr13", 120614378), new ReferenceInfo("chr14", 123978870), new ReferenceInfo("chr15", 103492577),
                    new ReferenceInfo("chr16", 98252459), new ReferenceInfo("chr17", 95177420), new ReferenceInfo("chr18", 90736837),
                    new ReferenceInfo("chr19", 61321190), new ReferenceInfo("chrX", 165556469), new ReferenceInfo("chrY", 16029404) })
            };
        }
    }

    /**
     * If we're a popular genome, we may come with a known set of popular genes.
     */
    public Gene[] getGenes() {
        return genes != null ? genes : new Gene[0];
    }

    public static class Cytoband {
        public final String chr;
        public final int start;
        public final int end;
        public final String name;
        final GiemsaStain stain;

        Cytoband(String[] tokens) {
            chr = tokens[0];
            start = Integer.parseInt(tokens[1]);
            end = Integer.parseInt(tokens[2]);
            name = tokens.length > 3 ? tokens[3] : null;
            if (tokens.length > 4) {
                stain = Enum.valueOf(GiemsaStain.class, tokens[4].toUpperCase());
            } else {
                stain = null;
            }
        }

        public Color getColor() {
            switch (stain) {
                case GPOS25:
                    return new Color(192, 192, 192);
                case GPOS50:
                    return new Color(128, 128, 128);
                case GPOS75:
                    return new Color(64, 64, 64);
                case GPOS100:
                    return new Color(0, 0, 0);
                default:
                    return new Color(255, 255, 255);
            }
        }
        
        public boolean isCentromere() {
            return stain == GiemsaStain.ACEN;
        }

        enum GiemsaStain {
            GNEG,
            GPOS50,
            GPOS75,
            GPOS25,
            GPOS100,
            ACEN,
            GVAR,
            STALK
        }
    }

    static class ReferenceInfo {

        String name;
        int length;

        public ReferenceInfo(String name, int length) {
            this.name = name;
            this.length = length;
        }
    }

    public static class Gene {
        public final String description;
        public final URI uri;

        Gene(String desc, URI uri) {
            this.description = desc;
            this.uri = uri;
        }

        @Override
        public String toString() {
            return description;
        }
    }
}
