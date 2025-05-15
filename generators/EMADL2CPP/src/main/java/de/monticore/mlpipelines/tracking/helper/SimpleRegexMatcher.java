package de.monticore.mlpipelines.tracking.helper;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * A simple regex matcher that supports multiple patterns and only '*' as wildcard.
 */
public class SimpleRegexMatcher {

    private final List<Pattern> patterns = new ArrayList<>();

    public SimpleRegexMatcher(List<? extends CharSequence> patterns) {
        for(CharSequence pattern : patterns) {
            addPattern(pattern);
        }
    }

    public SimpleRegexMatcher() {}

    public void addPattern(CharSequence simplePattern) {
        // Adding new tokens: https://stackoverflow.com/a/24337711
        Pattern converterPattern = Pattern.compile("[^*]+|(\\*)");
        Matcher matcher = converterPattern.matcher(simplePattern);

        StringBuffer buffer = new StringBuffer();
        while(matcher.find()) {
            if(matcher.group(1) != null) {
                matcher.appendReplacement(buffer, ".*");
            } else {
                matcher.appendReplacement(buffer, "\\\\Q" + matcher.group(0) + "\\\\E");
            }
        }
        matcher.appendTail(buffer);

        patterns.add(Pattern.compile(buffer.toString()));
    }

    /**
     * @param string The string to match against the patterns.
     * @return True if the string matches any of the patterns.
     */
    public boolean matchesAny(String string) {
        for(Pattern pattern : patterns) {
            if(pattern.matcher(string).matches()) {
                return true;
            }
        }
        return false;
    }

    /**
     * @param string The string to match against the patterns.
     * @return True if the string matches all the patterns.
     */
    public boolean matchesAll(String string) {
        for(Pattern pattern : patterns) {
            if(!pattern.matcher(string).matches()) {
                return false;
            }
        }
        return true;
    }



}
