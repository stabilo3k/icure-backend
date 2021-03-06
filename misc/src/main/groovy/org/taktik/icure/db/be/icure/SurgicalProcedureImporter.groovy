package org.taktik.icure.db.be.icure

import com.google.common.collect.Sets
import org.bouncycastle.jce.provider.BouncyCastleProvider
import org.ektorp.CouchDbInstance
import org.ektorp.DbAccessException
import org.ektorp.ViewQuery
import org.ektorp.http.HttpClient
import org.ektorp.http.StdHttpClient
import org.ektorp.impl.StdCouchDbInstance
import org.taktik.icure.db.Importer
import org.taktik.icure.entities.base.Code

import java.security.Security

class SurgicalProcedureImporter extends Importer{

    def language = 'fr'

    SurgicalProcedureImporter() {
        HttpClient httpClient = new StdHttpClient.Builder().socketTimeout(120000).connectionTimeout(120000).url("http://127.0.0.1:5984")/*.username("template").password("804e5824-8d79-4074-89be-def87278b51f")*/.build()
        CouchDbInstance dbInstance = new StdCouchDbInstance(httpClient);
        // if the second parameter is true, the database will be created if it doesn't exists
        couchdbBase = dbInstance.createConnector('icure-base', false);

        Security.addProvider(new BouncyCastleProvider())
    }

    static public void main(String... args) {
        def options = args.size() > 1 ? args[0..-2] : []

        def language = 'fr'
        def keyRoot = null
        def src_file = new File(args[-1])
        def type = src_file.name.replaceAll('.xml','');
        options.each {
            if (it.startsWith("lang=")) {
                language = it.substring(5);
            } else if (it.startsWith("keyroot=")) {
                keyRoot = it.substring(8);
            } else if (it.startsWith("type=")) {
                type = it.substring(5);
            }
        }


        def start = System.currentTimeMillis()

        def importer = new SurgicalProcedureImporter()

        importer.language = language;
        importer.keyRoot = keyRoot ?: importer.DEFAULT_KEY_DIR;

        src_file.withReader('UTF8') { r ->
            importer.doScan(r, type);
        }

        println "Process completed in ${(System.currentTimeMillis() - start) / 1000.0} seconds"
    }

    def doScan(Reader r, String type) {
        def codes = []

            def root = new XmlSlurper().parse(r)
            def version = root.'@version'.text()
            root.Procedure.each {
                def label = [:]
                label.fr = it.Label_FR.text();
                label.nl = it.Label_NL.text();

                def code = new Code(Sets.newHashSet('be', 'fr'), type, it.LINE_NBR.text(), version, label)

                def links = [];

                for (String k in ['CD-HCPARTY']) {
                    if (it[k].text().length()) it[k].text().split(';').each { links << "CD-HCPARTY|${it}|1".toString() }
                }
                /*
                for (String k in ['INAMI-CODE']) {
                    if (it[k].text().length()) it[k].text().replaceAll(/^([0-9]{4})/,'').split(' ').each { links << "INAMI-RIZIV|${it}|1.0".toString() }
                }*/

                if (links.size()) {
                    code.links = links
                }

                codes << code
            }
        def current = new HashSet()

        boolean retry = true;
        while (retry) {
            retry = false;
            try {
                couchdbBase.queryView(new ViewQuery(includeDocs: false).dbPath(couchdbBase.path()).designDocId("_design/Code").viewName("all"), Code.class).each {
                    current<<it.id
                }
            } catch (DbAccessException e) {
                retry = true;
            }
        }

        codes.findAll {!current.contains(it.id)}.collate(100).each {
            if (it.size()>0) { couchdbBase.executeBulk(it) }
        }
    }
}
