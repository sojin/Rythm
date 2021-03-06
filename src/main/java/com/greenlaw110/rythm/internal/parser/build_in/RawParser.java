package com.greenlaw110.rythm.internal.parser.build_in;

import com.greenlaw110.rythm.internal.Keyword;
import com.greenlaw110.rythm.internal.dialect.Rythm;
import com.greenlaw110.rythm.internal.parser.BlockCodeToken;
import com.greenlaw110.rythm.internal.parser.ParserBase;
import com.greenlaw110.rythm.spi.IContext;
import com.greenlaw110.rythm.spi.IParser;
import com.greenlaw110.rythm.utils.TextBuilder;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Parse @raw() {...}
 */
public class RawParser extends KeywordParserFactory {

    @Override
    public Keyword keyword() {
        return Keyword.RAW;
    }

    public IParser create(IContext ctx) {
        return new ParserBase(ctx) {
            public TextBuilder go() {
                Matcher m = ptn(dialect()).matcher(remain());
                if (!m.matches()) return null;
                step(m.group(1).length());
                return new BlockCodeToken("__ctx.pushEscape(com.greenlaw110.rythm.template.ITemplate.Escape.RAW);", ctx()) {
                    @Override
                    public void openBlock() {
                    }

                    @Override
                    public String closeBlock() {
                        return "__ctx.popEscape();";
                    }
                };
            }
        };
    }

    @Override
    protected String patternStr() {
        return "(%s%s\\s*\\(\\s*\\)[\\s]*\\{).*";
    }

    public static void main(String[] args) {
        Pattern p = new RawParser().ptn(new Rythm());
        Matcher m = p.matcher("@raw() {\n" +
                "    @body\n" +
                "}");
        if (m.find()) {
            System.out.println(m.group(1));
        }
    }

}
