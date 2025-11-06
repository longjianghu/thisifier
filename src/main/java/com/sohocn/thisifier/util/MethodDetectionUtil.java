package com.sohocn.thisifier.util;

import com.intellij.psi.*;
import com.intellij.psi.util.PsiTreeUtil;
import org.jetbrains.annotations.NotNull;

/**
 * The type Method detection util.
 *
 * @author longjianghu
 */
public class MethodDetectionUtil {
    /**
     * Check if the method call is an instance method of the current class
     *
     * @param methodCall method call expression
     * @param psiFile    current file
     * @return true if it is an instance method of the current class, false otherwise
     */
    public static boolean isCurrentClassInstanceMethod(@NotNull PsiMethodCallExpression methodCall, @NotNull PsiFile psiFile) {
        PsiReferenceExpression methodExpression = methodCall.getMethodExpression();

        if (methodExpression.getQualifierExpression() != null) {
            return false;
        }
        
        String methodName = methodExpression.getReferenceName();
        if (methodName == null) {
            return false;
        }
        
        PsiMethod resolvedMethod = methodCall.resolveMethod();
        if (resolvedMethod == null) {
            return false;
        }
        
        if (resolvedMethod.hasModifierProperty(PsiModifier.STATIC)) {
            return false;
        }
        
        PsiClass containingClass = PsiTreeUtil.getParentOfType(methodCall, PsiClass.class);
        if (containingClass == null) {
            return false;
        }
        
        PsiClass methodClass = resolvedMethod.getContainingClass();
        if (methodClass == null) {
            return false;
        }
        
        return containingClass.isEquivalentTo(methodClass);
    }
}