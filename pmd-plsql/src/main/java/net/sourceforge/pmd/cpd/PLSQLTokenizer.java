/**
 * BSD-style license; for more info see http://pmd.sourceforge.net/license.html
 */

package net.sourceforge.pmd.cpd;

import java.io.StringReader;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Pattern;

import net.sourceforge.pmd.lang.ast.SimpleCharStream;
import net.sourceforge.pmd.lang.plsql.ast.PLSQLParserConstants;
import net.sourceforge.pmd.lang.plsql.ast.PLSQLParserTokenManager;
import net.sourceforge.pmd.lang.plsql.ast.Token;

public class PLSQLTokenizer implements Tokenizer {
    private static final Logger LOGGER = Logger.getLogger(PLSQLTokenizer.class.getName());

    public static final String IGNORE_COMMENTS = "ignore_comments";
    public static final String IGNORE_IDENTIFIERS = "ignore_identifiers";
    public static final String IGNORE_LITERALS = "ignore_literals";
    
    private static final String EMPTY_STRING = "";

    private boolean ignoreComments;
    private boolean ignoreIdentifiers;
    private boolean ignoreLiterals;
    private Pattern skipBlocksPattern;
    
    public void setProperties(Properties properties) {
        /*
         * The Tokenizer is derived from PLDoc, in which comments are very
         * important When looking for duplication, we are probably not
         * interested in comment variation, so we shall default ignoreComments
         * to true
         */
        ignoreComments = Boolean.parseBoolean(properties.getProperty(IGNORE_COMMENTS, "true"));
        ignoreIdentifiers = Boolean.parseBoolean(properties.getProperty(IGNORE_IDENTIFIERS, "false"));
        ignoreLiterals = Boolean.parseBoolean(properties.getProperty(IGNORE_LITERALS, "false"));
        String skipBlocksPatternString = properties.getProperty(OPTION_SKIP_BLOCKS_PATTERN);
        if (skipBlocksPatternString != null) {
            skipBlocksPattern = Pattern.compile(skipBlocksPatternString, Pattern.MULTILINE | Pattern.DOTALL);
        }
    }

    public void setIgnoreComments(boolean ignore) {
        this.ignoreComments = ignore;
    }

    public void setIgnoreLiterals(boolean ignore) {
        this.ignoreLiterals = ignore;
    }

    public void setIgnoreIdentifiers(boolean ignore) {
        this.ignoreIdentifiers = ignore;
    }

    /**
     * Read Reader from SourceCode and output an ordered tree of PLSQL tokens.
     * 
     * @param sourceCode
     *            PLSQL source in file, string or database (any suitable object
     *            that can return a Reader).
     * @param tokenEntries
     *            Derived based on PLSQL Abstract Syntax Tree (derived from
     *            PLDOc parser.)
     */
    @Override
    public void tokenize(SourceCode sourceCode, Tokens tokenEntries) {
        long encounteredTokens = 0;
        long addedTokens = 0;

        if (LOGGER.isLoggable(Level.FINE)) {
            LOGGER.fine("PLSQLTokenizer: ignoreComments==" + ignoreComments);
            LOGGER.fine("PLSQLTokenizer: ignoreIdentifiers==" + ignoreIdentifiers);
            LOGGER.fine("PLSQLTokenizer: ignoreLiterals==" + ignoreLiterals);
        }

        String fileName = sourceCode.getFileName();
        StringBuilder sb = sourceCode.getCodeBuffer();

        String replaced = sb.toString();
        if (skipBlocksPattern != null) {
            LOGGER.fine("PLSQLTokenizer: skipBlocksPattern==" + skipBlocksPattern.toString());
            replaced = skipBlocksPattern.matcher(replaced).replaceAll(EMPTY_STRING);
        }

        PLSQLParserTokenManager tokenMgr = new PLSQLParserTokenManager(new SimpleCharStream(new StringReader(replaced)));
        Token currentToken = tokenMgr.getNextToken();
        while (currentToken.image.length() > 0) {
            String image = currentToken.image;

            encounteredTokens++;
            if (ignoreComments && (currentToken.kind == PLSQLParserConstants.SINGLE_LINE_COMMENT
                    || currentToken.kind == PLSQLParserConstants.MULTI_LINE_COMMENT
                    || currentToken.kind == PLSQLParserConstants.FORMAL_COMMENT
                    || currentToken.kind == PLSQLParserConstants.COMMENT
                    || currentToken.kind == PLSQLParserConstants.IN_MULTI_LINE_COMMENT
                    || currentToken.kind == PLSQLParserConstants.IN_FORMAL_COMMENT)) {
                image = String.valueOf(currentToken.kind);
            }

            if (ignoreIdentifiers && currentToken.kind == PLSQLParserConstants.IDENTIFIER) {
                image = String.valueOf(currentToken.kind);
            }

            if (ignoreLiterals && (currentToken.kind == PLSQLParserConstants.UNSIGNED_NUMERIC_LITERAL
                    || currentToken.kind == PLSQLParserConstants.FLOAT_LITERAL
                    || currentToken.kind == PLSQLParserConstants.INTEGER_LITERAL
                    || currentToken.kind == PLSQLParserConstants.CHARACTER_LITERAL
                    || currentToken.kind == PLSQLParserConstants.STRING_LITERAL
                    || currentToken.kind == PLSQLParserConstants.QUOTED_LITERAL)) {
                image = String.valueOf(currentToken.kind);
            }

            tokenEntries.add(new TokenEntry(image, fileName, currentToken.beginLine));
            addedTokens++;
            currentToken = tokenMgr.getNextToken();
        }
        tokenEntries.add(TokenEntry.getEOF());
        if (LOGGER.isLoggable(Level.FINE)) {
            LOGGER.fine(sourceCode.getFileName() + ": encountered " + encounteredTokens + " tokens;" + " added "
                    + addedTokens + " tokens");
        }
    }

}
