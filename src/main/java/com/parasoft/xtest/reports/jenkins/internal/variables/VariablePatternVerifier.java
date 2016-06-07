/*
* (C) Copyright ParaSoft Corporation 2013. All rights reserved.
* THIS IS UNPUBLISHED PROPRIETARY SOURCE CODE OF ParaSoft
* The copyright notice above does not evidence any
* actual or intended publication of such source code.
*/


package com.parasoft.xtest.reports.jenkins.internal.variables;

import java.util.HashSet;
import java.util.Set;

/** 
 * Checks variables for correctness.
 */
public class VariablePatternVerifier
{
    private final String _pattern;
    
    private String _errorMessage = null;
    
    /**
     * Create verifier for given pattern.
     * @param pattern
     */
    public VariablePatternVerifier(String pattern)
    {
        _pattern = pattern == null ? "" : pattern; //$NON-NLS-1$
    }

    /**
     * @return true if pattern contains variable in any of notations ('%' or '$' based).
     */
    public boolean containsVariables()
    {
        return _pattern.contains("%") || _pattern.contains("$"); //$NON-NLS-1$ //$NON-NLS-2$
    }
    
    /**
     * @return false if variables seem to be incorrectly specified, or unsupported variable is used. 
     * True if there are no variables or they are all recognized.
     * See also {@link JenkinsVariablesResolver#getResolvableVariables()}. 
     * Call prior to {@link #getErrorMessage()}.
     */
    public boolean checkVariableNotation()
    {
        int occ = countOccurrences("%"); //$NON-NLS-1$
        if (occ % 2 == 1) {
            _errorMessage = Messages.PERCENT_SIGN_MISSING;
            return false;
        }
        int dollarCount = countOccurrences("$"); //$NON-NLS-1$
        if ((dollarCount != countOccurrences("${")) || (dollarCount != countOccurrences("}"))) { //$NON-NLS-1$ //$NON-NLS-2$
            _errorMessage = Messages.CURLY_BRACKET_MISSING;
            return false;
        }
        Set<String> usedVariables = getUsedVariables();
        Set<String> resolvableVariables = JenkinsVariablesResolver.getResolvableVariables();
        for (String variable : usedVariables) {
            if (!resolvableVariables.contains(variable)) {
                _errorMessage = "Unrecognized variable used: " + variable;   //$NON-NLS-1$
                return false;
            }
        }
        _errorMessage = null;
        return true;
    }
    
    /**
     * @return error message bound while processing {@link #checkVariableNotation()} or null.
     */
    public String getErrorMessage()
    {
        return _errorMessage;
    }

    private Set<String> getUsedVariables()
    {
        Set<String> result = new HashSet<String>();
        addVariables(result, "%", "%"); //$NON-NLS-1$ //$NON-NLS-2$
        addVariables(result, "${", "}"); //$NON-NLS-1$ //$NON-NLS-2$
        return result;
    }

    private void addVariables(Set<String> result, String firstSign, String secondSign)
    {
        String expr = _pattern;
        int firstSignIndex = expr.indexOf(firstSign);
        while (firstSignIndex >= 0) {
            expr = expr.substring(firstSignIndex + firstSign.length());
            int secondSignIndex = expr.indexOf(secondSign);
            result.add(expr.substring(0, secondSignIndex));
            if (secondSignIndex > expr.length()) {
                break;
            }
            expr = expr.substring(secondSignIndex + secondSign.length());
            firstSignIndex = expr.indexOf(firstSign);
        }
    }

    private int countOccurrences(String expr)
    {
        int count = _pattern.length() - _pattern.replace(expr, "").length(); //$NON-NLS-1$
        return count / expr.length();
    }

}
