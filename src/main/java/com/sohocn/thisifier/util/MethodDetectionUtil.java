package com.sohocn.thisifier.util;

import com.intellij.psi.*;
import com.intellij.psi.util.PsiTreeUtil;

/**
 * Utility class for detecting and analyzing method calls
 */
public class MethodDetectionUtil {
    
    /**
     * Check if the given element is a method call that can have 'this' added
     */
    public static boolean isEligibleForThisAddition(PsiElement element) {
        if (!(element instanceof PsiMethodCallExpression methodCall)) {
            return false;
        }
        
        // Check if it already has 'this.' or any qualifier
        PsiExpression qualifier = methodCall.getMethodExpression().getQualifierExpression();
        if (qualifier != null) {
            return false;
        }
        
        // Check if it's a static method call
        PsiMethod method = methodCall.resolveMethod();
        if (method != null && method.hasModifierProperty(PsiModifier.STATIC)) {
            return false;
        }
        
        // Check if it's a method from the current class (not inherited)
        PsiClass containingClass = PsiTreeUtil.getParentOfType(methodCall, PsiClass.class);
        if (containingClass == null) {
            return false;
        }
        
        if (method != null) {
            PsiClass methodClass = method.getContainingClass();
            if (methodClass == null || !methodClass.equals(containingClass)) {
                return false; // Method from different class (inherited or external)
            }
        }
        
        return true;
    }
    
    /**
     * Get the method name from the method call
     */
    public static String getMethodName(PsiMethodCallExpression methodCall) {
        return methodCall.getMethodExpression().getReferenceName();
    }
    
    /**
     * Get the containing class of the method call
     */
    public static PsiClass getContainingClass(PsiMethodCallExpression methodCall) {
        return PsiTreeUtil.getParentOfType(methodCall, PsiClass.class);
    }
}