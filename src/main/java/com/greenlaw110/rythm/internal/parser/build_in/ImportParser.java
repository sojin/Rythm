package com.greenlaw110.rythm.internal.parser.build_in;

import com.greenlaw110.rythm.internal.CodeBuilder;
import com.greenlaw110.rythm.internal.Keyword;
import com.greenlaw110.rythm.internal.parser.Directive;
import com.greenlaw110.rythm.internal.parser.ParserBase;
import com.greenlaw110.rythm.spi.IContext;
import com.greenlaw110.rythm.spi.IParser;
import com.greenlaw110.rythm.utils.TextBuilder;

import java.util.regex.Matcher;

public class ImportParser extends KeywordParserFactory {

    private static final String R = "(%s%s[\\s]+([a-zA-Z0-9_\\.*,\\s]+)(;|\\r?\\n)+).*";

    public ImportParser() {
    }

    protected String patternStr() {
        return R;
    }

    public IParser create(IContext c) {
        return new ParserBase(c) {
            public TextBuilder go() {
                Matcher m = ptn(dialect()).matcher(remain());
                if (!m.matches()) return null;
                String s = m.group(1);
                step(s.length());
                //String imports = s.replaceFirst(String.format("%s%s[\\s]+", a(), keyword()), "").replaceFirst("(;|\\r?\\n)+$", "");
                s = m.group(2);
                /**
                 * We need to make sure import path added to template class
                 * to support call tag using import paths. That why we move
                 * the addImport statement here from Directive.call()
                 */
                 String[] sa = s.split("[,\\s]+");
                 CodeBuilder cb = builder();
                boolean statik = false;
                for (String imp: sa) {
                    if ("static".equals(imp)) statik = true;
                    else {
                        cb.addImport(statik ? "static " + imp : imp);
                        statik = false;
                    }
                }
                return new Directive("", ctx());
            }
        };
    }

    @Override
    public Keyword keyword() {
        return Keyword.IMPORT;
    }

}
