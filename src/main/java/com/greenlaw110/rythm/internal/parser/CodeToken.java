package com.greenlaw110.rythm.internal.parser;

import com.greenlaw110.rythm.spi.IContext;
import com.greenlaw110.rythm.spi.Token;


public class CodeToken extends Token {
    public CodeToken(String s, IContext context) {
        super(s, context);
    }

    @Override
    public void output() {
        p(s);
        pline();
    }
}
