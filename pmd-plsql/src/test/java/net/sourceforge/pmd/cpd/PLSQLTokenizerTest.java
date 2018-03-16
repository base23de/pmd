/**
 * BSD-style license; for more info see http://pmd.sourceforge.net/license.html
 */

package net.sourceforge.pmd.cpd;

import java.io.IOException;
import java.util.Properties;

import org.apache.commons.io.IOUtils;
import org.junit.Before;
import org.junit.Test;

import net.sourceforge.pmd.testframework.AbstractTokenizerTest;

public class PLSQLTokenizerTest extends AbstractTokenizerTest {

    private static final String FILENAME = "sample-plsql.sql";
    private static final String FILENAME_ADVANCED = "sample-plsql-advanced.sql";

    @Before
    @Override
    public void buildTokenizer() throws IOException {
        this.tokenizer = new PLSQLTokenizer();
        this.sourceCode = new SourceCode(new SourceCode.StringCodeLoader(this.getSampleCode(), FILENAME));
    }

    @Override
    public String getSampleCode() throws IOException {
        return IOUtils.toString(PLSQLTokenizer.class.getResourceAsStream(FILENAME));
    }

    @Test
    public void tokenizeTest() throws IOException {
        this.expectedTokenCount = 1422;
        super.tokenizeTest();
    }

    @Test
    public void skipBlocksTokenizeTest() throws IOException {
        final Properties properties = new Properties();
        properties.setProperty(Tokenizer.OPTION_SKIP_BLOCKS_PATTERN, "^\\s*log.log_parameter_einfuegen.+?\\);\\s*$");
        ((PLSQLTokenizer) this.tokenizer).setProperties(properties);
        this.sourceCode = new SourceCode(new SourceCode.StringCodeLoader(IOUtils.toString(PLSQLTokenizer.class.getResourceAsStream(FILENAME_ADVANCED)), FILENAME_ADVANCED));
        this.expectedTokenCount = 121;
        super.tokenizeTest();
    }

    public static junit.framework.Test suite() {
        return new junit.framework.JUnit4TestAdapter(PLSQLTokenizerTest.class);
    }
}
