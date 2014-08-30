/*
 * Copyright (c) 2013-2014, Centre for Genomic Regulation (CRG).
 * Copyright (c) 2013-2014, Paolo Di Tommaso and the respective authors.
 *
 *   This file is part of 'Nextflow'.
 *
 *   Nextflow is free software: you can redistribute it and/or modify
 *   it under the terms of the GNU General Public License as published by
 *   the Free Software Foundation, either version 3 of the License, or
 *   (at your option) any later version.
 *
 *   Nextflow is distributed in the hope that it will be useful,
 *   but WITHOUT ANY WARRANTY; without even the implied warranty of
 *   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *   GNU General Public License for more details.
 *
 *   You should have received a copy of the GNU General Public License
 *   along with Nextflow.  If not, see <http://www.gnu.org/licenses/>.
 */

package nextflow.extension
import java.nio.file.Path
import java.util.concurrent.locks.Lock
import java.util.concurrent.locks.ReentrantLock
import java.util.regex.Pattern

import groovy.util.logging.Slf4j
import groovyx.gpars.agent.Agent
import nextflow.file.FileHelper
import nextflow.util.Duration
import org.apache.commons.lang.StringUtils
import org.codehaus.groovy.runtime.DefaultGroovyMethods
import org.codehaus.groovy.runtime.ResourceGroovyMethods
import org.codehaus.groovy.runtime.StringGroovyMethods
/**
 * Provides extension methods to chunk text and file
 *
 * See more about extension methods
 * http://docs.codehaus.org/display/GROOVY/Creating+an+extension+module
 *
 * @author Paolo Di Tommaso <paolo.ditommaso@gmail.com>
 */

@Slf4j
class NextflowExtensions {

    static private Pattern PATTERN_RIGHT_TRIM = ~/\s+$/

    static private Pattern PATTERN_LEFT_TRIM = ~/^\s+/

    /**
     * Remove the left side after a dot (including it) e.g.
     * <pre>
     *     0.10     => 0
     *     10000.00 => 10000
     * </pre>
     *
     * @param self
     * @return
     */
    static String trimDotZero(String self) {
        int p = self?.indexOf('.')
        p!=-1 ? self.substring(0,p) : self
    }

    /**
     * Remove blank chars at the end of the string
     *
     * @param self The string itself
     * @return The string with blanks removed
     */

    static String rightTrim(String self) {
        self.replaceAll(PATTERN_RIGHT_TRIM,'')
    }

    /**
     * Remove blank chars at string beginning
     *
     * @param self The string itself
     * @return The string with blanks removed
     */
    static String leftTrim( String self ) {
        self.replaceAll(PATTERN_LEFT_TRIM,'')
    }

    /**
     * <p>Strips any of a set of characters from the start and end of a String.
     * This is similar to {@link String#trim()} but allows the characters
     * to be stripped to be controlled.</p>
     *
     * <p>A <code>null</code> input String returns <code>null</code>.
     * An empty string ("") input returns the empty string.</p>
     *
     * <p>If the stripChars String is <code>null</code>, whitespace is
     * stripped as defined by {@link Character#isWhitespace(char)}.
     * Alternatively use {@link #strip(String)}.</p>
     *
     * <pre>
     * StringUtils.strip(null, *)          = null
     * StringUtils.strip("", *)            = ""
     * StringUtils.strip("abc", null)      = "abc"
     * StringUtils.strip("  abc", null)    = "abc"
     * StringUtils.strip("abc  ", null)    = "abc"
     * StringUtils.strip(" abc ", null)    = "abc"
     * StringUtils.strip("  abcyx", "xyz") = "  abc"
     * </pre>
     *
     * @param str  the String to remove characters from, may be null
     * @param stripChars  the characters to remove, null treated as whitespace
     * @return the stripped String, <code>null</code> if null String input
     */
    static strip( String self, String stripChars = null ) {
        StringUtils.strip(self, stripChars)
    }

    /**
     * <p>Strips any of a set of characters from the start of a String.</p>
     *
     * <p>A <code>null</code> input String returns <code>null</code>.
     * An empty string ("") input returns the empty string.</p>
     *
     * <p>If the stripChars String is <code>null</code>, whitespace is
     * stripped as defined by {@link Character#isWhitespace(char)}.</p>
     *
     * <pre>
     * StringUtils.stripStart(null, *)          = null
     * StringUtils.stripStart("", *)            = ""
     * StringUtils.stripStart("abc", "")        = "abc"
     * StringUtils.stripStart("abc", null)      = "abc"
     * StringUtils.stripStart("  abc", null)    = "abc"
     * StringUtils.stripStart("abc  ", null)    = "abc  "
     * StringUtils.stripStart(" abc ", null)    = "abc "
     * StringUtils.stripStart("yxabc  ", "xyz") = "abc  "
     * </pre>
     *
     * @param str  the String to remove characters from, may be null
     * @param stripChars  the characters to remove, null treated as whitespace
     * @return the stripped String, <code>null</code> if null String input
     */
    static stripStart( String self, String stripChars = null ) {
        StringUtils.stripStart(self, stripChars)
    }

    /**
     * <p>Strips any of a set of characters from the end of a String.</p>
     *
     * <p>A <code>null</code> input String returns <code>null</code>.
     * An empty string ("") input returns the empty string.</p>
     *
     * <p>If the stripChars String is <code>null</code>, whitespace is
     * stripped as defined by {@link Character#isWhitespace(char)}.</p>
     *
     * <pre>
     * StringUtils.stripEnd(null, *)          = null
     * StringUtils.stripEnd("", *)            = ""
     * StringUtils.stripEnd("abc", "")        = "abc"
     * StringUtils.stripEnd("abc", null)      = "abc"
     * StringUtils.stripEnd("  abc", null)    = "  abc"
     * StringUtils.stripEnd("abc  ", null)    = "abc"
     * StringUtils.stripEnd(" abc ", null)    = " abc"
     * StringUtils.stripEnd("  abcyx", "xyz") = "  abc"
     * StringUtils.stripEnd("120.00", ".0")   = "12"
     * </pre>
     *
     * @param str  the String to remove characters from, may be null
     * @param stripChars  the set of characters to remove, null treated as whitespace
     * @return the stripped String, <code>null</code> if null String input
     */
    static stripEnd( String self, String stripChars = null ) {
        StringUtils.stripEnd(self, stripChars)
    }

    /**
     * <p>Capitalizes a String changing the first letter to title case as
     * per {@link Character#toTitleCase(char)}. No other letters are changed.</p>
     *
     * <p>For a word based algorithm, see {@link org.apache.commons.lang.WordUtils#capitalize(String)}.
     * A <code>null</code> input String returns <code>null</code>.</p>
     *
     * <pre>
     * StringUtils.capitalize(null)  = null
     * StringUtils.capitalize("")    = ""
     * StringUtils.capitalize("cat") = "Cat"
     * StringUtils.capitalize("cAt") = "CAt"
     * </pre>
     *
     */
    static String capitalize(String self) {
        StringUtils.capitalize(self)
    }

    /**
     * <p>Uncapitalizes a String changing the first letter to title case as
     * per {@link Character#toLowerCase(char)}. No other letters are changed.</p>
     *
     * <p>For a word based algorithm, see {@link org.apache.commons.lang.WordUtils#uncapitalize(String)}.
     * A <code>null</code> input String returns <code>null</code>.</p>
     *
     * <pre>
     * StringUtils.uncapitalize(null)  = null
     * StringUtils.uncapitalize("")    = ""
     * StringUtils.uncapitalize("Cat") = "cat"
     * StringUtils.uncapitalize("CAT") = "cAT"
     * </pre>
     *
     */
    static String uncapitalize(String self) {
        StringUtils.uncapitalize(self)
    }


    static private Pattern getPattern( obj ) {

        if( obj instanceof Map ) {
            if( obj.containsKey('pattern') )
                obj = obj.pattern
            else
                return null
        }

        if( obj instanceof Pattern ) {
            return obj
        }

        if( obj instanceof String ) {
            Pattern.compile(Pattern.quote(obj))
        }

        if( obj != null )
            throw new IllegalArgumentException()

        return null
    }



    /**
     * INTERNAL ONLY API
     * <p>
     * Add the {@code update} method to an {@code Agent} so that it call implicitly
     * the {@code Agent#updateValue} method
     *
     */

    static void update( Agent self, Closure message ) {
        assert message != null

        self.send {
            message.call(it)
            updateValue(it)
        }

    }

    /**
     * Invokes the specify closure including it with a lock/unlock calls pair
     *
     * @param self
     * @param interruptible
     * @param closure
     * @return the closure result
     */
    static <T> T withLock( Lock self, boolean interruptible = false, Closure<T> closure ) {
        // acquire the lock
        if( interruptible )
            self.lockInterruptibly()
        else
            self.lock()

        try {
            return closure.call()
        }
        finally {
            self.unlock();
        }
    }

    /**
     * Invokes the specify closure only if it is able to acquire a lock
     *
     * @param self
     * @param interruptible
     * @param closure
     * @return the closure result
     */
    static boolean tryLock( Lock self, Closure closure ) {
        if( !self.tryLock() )
            return false

        try {
            closure.call()
        }
        finally {
            self.unlock()
            return true
        }
    }


    /**
     * Converts a {@code String} to a {@code Duration} object
     *
     * @param self
     * @param type
     * @return
     */
    static def asType( String self, Class type ) {
        if( type == Duration ) {
            return new Duration(self)
        }
        else if( Path.isAssignableFrom(type) ) {
            return FileHelper.asPath(self)
        }

        StringGroovyMethods.asType(self, type);
    }

    /**
     * Converts a {@code GString} to a {@code Duration} object
     *
     * @param self
     * @param type
     * @return
     */
    static def asType( GString self, Class type ) {
        if( type == Duration ) {
            return new Duration(self.toString())
        }
        else if( Path.isAssignableFrom(type) ) {
            return FileHelper.asPath(self)
        }

        StringGroovyMethods.asType(self, type);
    }

    /**
     * Converts a {@code Number} to a {@code Duration} object
     *
     * @param self
     * @param type
     * @return
     */
    static def asType( Number self, Class type ) {
        if( type == Duration ) {
            return new Duration(self.longValue())
        }

        DefaultGroovyMethods.asType(self, type);
    }

    /**
     * Converts a {@code File} to a {@code Path} object
     *
     * @param self
     * @param type
     * @return
     */
    static def asType( File self, Class type ) {
        if( Path.isAssignableFrom(type) ) {
            return self.toPath()
        }

        ResourceGroovyMethods.asType(self, type);
    }


    private static Lock MAP_LOCK = new ReentrantLock()

    static def <T> T getOrCreate( Map self, key, factory ) {

        if( self.containsKey(key) )
            return (T)self.get(key)

        MAP_LOCK.withLock {
            if( self.containsKey(key) )
                return self.get(key)

            def result = factory instanceof Closure ? factory.call(key) : factory
            self.put(key,result)
            return result
        }

    }


}
