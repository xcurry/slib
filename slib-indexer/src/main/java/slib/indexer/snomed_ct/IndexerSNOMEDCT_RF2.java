/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package slib.indexer.snomed_ct;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.util.HashSet;
import java.util.Set;
import java.util.regex.Pattern;
import org.openrdf.model.URI;
import org.openrdf.model.Value;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import slib.indexer.IndexElementBasic;
import slib.indexer.IndexHash;
import slib.sglib.model.graph.G;
import slib.sglib.model.repo.DataFactory;
import slib.utils.ex.SLIB_Ex_Critic;
import slib.utils.ex.SLIB_Exception;

/**
 *
 * @author Harispe Sébastien <harispe.sebastien@gmail.com>
 */
public class IndexerSNOMEDCT_RF2 {

    private int DESCRIPTION_CONCEPT_ID = 4;
    private int DESCRIPTION_ACTIVE = 2;
    private int DESCRIPTION_TERM = 7;
    Logger logger = LoggerFactory.getLogger(this.getClass());
    Pattern p_tab = Pattern.compile("\\t");
    DataFactory repo;

    /**
     * Only load an index for the URI already loaded
     *
     * @param description_file
     * @param defaultNamespace
     * @return
     * @throws SLIB_Exception
     */
    public IndexHash buildIndex(DataFactory factory, String description_file, String defaultNamespace) throws SLIB_Exception {

        repo = factory;
        logger.info("Building Index");
        logger.info("Description file: " + description_file);

        IndexHash index = new IndexHash();

        FileInputStream fstream;
        try {
            fstream = new FileInputStream(description_file);

            DataInputStream in = new DataInputStream(fstream);
            BufferedReader br = new BufferedReader(new InputStreamReader(in));

            String line;
            String[] split;

            while ((line = br.readLine()) != null) {

                split = p_tab.split(line);

                boolean active = split[DESCRIPTION_ACTIVE].trim().equals("1");

                if (active) {

                    URI cURI = repo.createURI(defaultNamespace + split[DESCRIPTION_CONCEPT_ID]);


                    if (repo.getURI(cURI) != null) {

                        if (index.getMapping().containsKey(cURI)) {

                            index.getMapping().get(cURI).addDescription(split[DESCRIPTION_TERM]);


                        } else {
                            IndexElementBasic i = new IndexElementBasic(split[DESCRIPTION_CONCEPT_ID]);
                            i.addDescription(split[DESCRIPTION_TERM]);
                            index.getMapping().put(cURI, i);
                        }
                    }
                }
            }
            in.close();

            logger.info("Process Done");

        } catch (Exception ex) {
            throw new SLIB_Ex_Critic(ex.getMessage());
        }

        return index;
    }

    /**
     * Same as builIndex removing the index not associated to a loaded vertex
     *
     * @see #buildIndex(slib.sglib.model.repo.DataFactory, java.lang.String,
     * java.lang.String)
     * @param factory
     * @param description_file
     * @param defaultNamespace
     * @param graph
     * @return
     * @throws SLIB_Exception
     */
    public IndexHash buildIndex(DataFactory factory, String description_file, String defaultNamespace, G graph) throws SLIB_Exception {

        logger.info("Building Index");
        IndexHash index = buildIndex(factory, description_file, defaultNamespace);

        logger.info("Cleaning Index");
        Set<Value> toRemove = new HashSet<Value>();
        for (Value k : index.getMapping().keySet()) {

            if (!graph.containsVertex(k)) {
                toRemove.add(k);
            }
        }
        for (Value v : toRemove) {
            index.getMapping().remove(v);
        }
        logger.info("Done");
        return index;

    }
}
