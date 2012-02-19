package com.greenlaw110.rythm.internal.compiler;

import com.greenlaw110.rythm.RythmEngine;
import com.greenlaw110.rythm.logger.ILogger;
import com.greenlaw110.rythm.logger.Logger;
import com.greenlaw110.rythm.spi.ITemplateClassEnhancer;

import java.io.*;
import java.security.MessageDigest;

/**
 * Used to speed up compilation time
 */
public class TemplateClassCache {
    private static final ILogger logger = Logger.get(TemplateClassCache.class);

    private RythmEngine engine = null;

    public TemplateClassCache(RythmEngine engine) {
        this.engine = engine;
    }

    /**
     * Delete the bytecode
     * @param tc The template class
     */
    public void deleteCache(TemplateClass tc) {
        try {
            File f = getCacheFile(tc);
            if (f.exists()) {
                f.delete();
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Retrieve java source and bytecode if template content has not changed
     * @param tc
     */
    public void loadTemplateClass(TemplateClass tc) {
        if (!engine.cacheEnabled()) {
            // cannot handle the v version scheme
            return;
        }
        try {
            File f = getCacheFile(tc);
            if (!f.exists() || !f.canRead()) return;
            InputStream is = new BufferedInputStream(new FileInputStream(f));

            // --- check hash
            int offset = 0;
            int read = -1;
            StringBuilder hash = new StringBuilder();
            while ((read = is.read()) != 0) {
                hash.append((char) read);
                offset++;
            }
            String curHash = hash(tc);
            if (!curHash.equals(hash.toString())) {
                if (logger.isTraceEnabled()) logger.trace("Bytecode too old (%s != %s)", hash, curHash);
                return;
            }
            
            // --- load java source
            read = -1;
            StringBuilder source = new StringBuilder();
            while ((read = is.read()) != 0) {
                source.append((char)read);
                offset++;
            }
            if (source.length() != 0) {
                tc.javaSource = source.toString();
            } // else it must be an inner class

//            // --- load version info
//            while ((read = is.read()) != 0) {
//                if (engine.isDevMode() && engine.hotswapAgent == null && !tc.isInner()) {
//                    tc.setVersion(read);
//                }
//                offset++;
//            }

            // --- load byte code
            byte[] byteCode = new byte[(int) f.length() - (offset + 2)];
            is.read(byteCode);
            tc.compiled(byteCode);

            is.close();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
    
    public void cacheTemplateClassSource(TemplateClass tc) {
        if (!engine.cacheEnabled()) {
            return;
        }
        try {
            File f = getCacheSourceFile(tc);
            OutputStream os = new BufferedOutputStream(new FileOutputStream(f));
            os.write(tc.javaSource.getBytes("utf-8"));
            os.close();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
    
    public void cacheTemplateClass(TemplateClass tc) {
        if (!engine.cacheEnabled()) {
            return;
        }
        String hash = hash(tc);
        try {
            File f = getCacheFile(tc);
            // --- write hash value
            OutputStream os = new BufferedOutputStream(new FileOutputStream(f));
            os.write(hash.getBytes("utf-8"));

            // --- cache java source
            os.write(0);
            if (null != tc.javaSource) {
                os.write(tc.javaSource.getBytes("utf-8"));
            } // else the tc is an inner class thus we don't have javaSource at all

//            // --- cache class version
//            os.write(0);
//            if (engine.reloadByIncClassVersion() && !tc.isInner()) {
//                // find out version number
//                final String sep = TemplateClass.CN_SUFFIX + "v";
//                String cn = tc.name();
//                int pos = cn.lastIndexOf(sep);
//                String sv = cn.substring(pos + sep.length());
//                int nv = Integer.valueOf(sv);
//                os.write(nv);
//            }

            // --- cache byte code
            os.write(0);
            //if (null != tc.enhancedByteCode) {
                os.write(tc.enhancedByteCode);
            //}
            os.close();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Build a hash of the source code.
     * To efficiently track source code modifications.
     */
    String hash(TemplateClass tc) {
        try {
            StringBuffer enhancers = new StringBuffer();
            for(ITemplateClassEnhancer plugin : engine.templateClassEnhancers) {
                enhancers.append(plugin.getClass().getName());
            }
            MessageDigest messageDigest = MessageDigest.getInstance("MD5");
            messageDigest.reset();
            messageDigest.update((RythmEngine.version + enhancers.toString() + tc.getTemplateSource(true)).getBytes("utf-8"));
            byte[] digest = messageDigest.digest();
            StringBuilder builder = new StringBuilder();
            for (int i = 0; i < digest.length; ++i) {
                int value = digest[i];
                if (value < 0) {
                    value += 256;
                }
                builder.append(Integer.toHexString(value));
            }
            return builder.toString();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
    
    String cacheFileName(TemplateClass tc, String suffix) {
        return tc.name0() + suffix;
    }
    
    /**
     * Retrieve the real file that will be used as cache.
     */
    File getCacheFile(TemplateClass tc) {
        String id = cacheFileName(tc, ".rythm");
        return new File(engine.tmpDir, id);
    }
    
    File getCacheSourceFile(TemplateClass tc) {
        String id = cacheFileName(tc, ".java");
        return new File(engine.tmpDir, id);
    }
    
}
