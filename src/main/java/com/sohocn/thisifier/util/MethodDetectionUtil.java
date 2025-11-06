package com.sohocn.thisifier.util;

import com.intellij.psi.*;
import com.intellij.psi.util.PsiTreeUtil;
import org.jetbrains.annotations.NotNull;

public class MethodDetectionUtil {

    /**
     * Check if the method call is an instance method of the current class
     * 
     * @param methodCall method call expression
     * @param psiFile current file
     * @return true if it is an instance method of the current class, false otherwise
     */
    public static boolean isCurrentClassInstanceMethod(@NotNull PsiMethodCallExpression methodCall, @NotNull PsiFile psiFile) {
        try {
            // Check if there is a 'this' prefix - if so, this method should be ignored
            PsiReferenceExpression methodExpression = methodCall.getMethodExpression();
            if (methodExpression == null) {
                return false;
            }
            
            if (methodExpression.getQualifierExpression() != null) {
                // If there is already a qualifier, this method should be ignored
                return false;
            }
            
            // Get the called method name
            String methodName = methodExpression.getReferenceName();
            if (methodName == null) {
                return false;
            }
            
            // Resolve the called method
            PsiMethod resolvedMethod = methodCall.resolveMethod();
            if (resolvedMethod == null) {
                return false;
            }
            
            // Check if the method is static
            if (resolvedMethod.hasModifierProperty(PsiModifier.STATIC)) {
                return false;
            }
            
            // Get the class containing this method call
            PsiClass containingClass = PsiTreeUtil.getParentOfType(methodCall, PsiClass.class);
            if (containingClass == null) {
                return false;
            }
            
            // Get the class where the called method is located
            PsiClass methodClass = resolvedMethod.getContainingClass();
            if (methodClass == null) {
                return false;
            }
            
            // Check if the called method is defined in the current class (not inherited)
            return containingClass.isEquivalentTo(methodClass);
        } catch (Exception e) {
            // Log the exception but don't let it break the functionality
            e.printStackTrace();
            return false;
        }
    }
    
    /**
     * Check if the qualifier is 'this'
     * 
     * @param qualifierExpression qualifier expression
     * @return true if it is 'this', false otherwise
     */
    /*private static boolean isThisQualifier(PsiExpression qualifierExpression) {
        if (qualifierExpression instanceof PsiThisExpression) {
            return true;
        }
        
        if (qualifierExpression instanceof PsiReferenceExpression) {
            PsiReferenceExpression referenceExpression = (PsiReferenceExpression) qualifierExpression;
            return "this".equals(referenceExpression.getText());
        }
        
        return false;
    }*/
}