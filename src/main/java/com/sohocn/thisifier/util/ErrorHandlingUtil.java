package com.sohocn.thisifier.util;

import com.intellij.openapi.diagnostic.Logger;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiMethodCallExpression;

/**
 * Utility class for error handling and logging
 */
public class ErrorHandlingUtil {
    private static final Logger LOG = Logger.getInstance(ErrorHandlingUtil.class);
    
    /**
     * Safely perform method call analysis with error handling
     */
    public static boolean safeIsEligibleForThisAddition(PsiElement element) {
        try {
            return MethodDetectionUtil.isEligibleForThisAddition(element);
        } catch (Exception e) {
            LOG.warn("Error while checking eligibility for 'this' addition", e);
            return false;
        }
    }
    
    /**
     * Safely get method name with error handling
     */
    public static String safeGetMethodName(PsiMethodCallExpression methodCall) {
        try {
            return MethodDetectionUtil.getMethodName(methodCall);
        } catch (Exception e) {
            LOG.warn("Error while getting method name", e);
            return null;
        }
    }
    
    /**
     * Log plugin operation for debugging and monitoring
     */
    public static void logOperation(String operation, PsiElement element) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("Thisifier: " + operation + " - " + 
                     (element != null ? element.getText() : "null element"));
        }
    }
    
    /**
     * Validate method call before processing
     */
    public static boolean isValidMethodCall(PsiMethodCallExpression methodCall) {
        if (methodCall == null) {
            return false;
        }
        
        if (!methodCall.isValid()) {
            LOG.warn("Invalid method call element detected");
            return false;
        }
        
        if (methodCall.getMethodExpression() == null) {
            LOG.warn("Method call without method expression");
            return false;
        }
        
        return true;
    }
}