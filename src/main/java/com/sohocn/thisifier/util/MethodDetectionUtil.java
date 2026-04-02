package com.sohocn.thisifier.util;

import com.intellij.psi.*;
import com.intellij.psi.util.PsiTreeUtil;
import org.jetbrains.annotations.NotNull;

import java.util.HashSet;
import java.util.Set;

/**
 * The type Method detection util.
 *
 * @author longjianghu
 */
public class MethodDetectionUtil {

    private static final Set<String> INJECTION_ANNOTATIONS = new HashSet<>();

    static {
        INJECTION_ANNOTATIONS.add("javax.annotation.Resource");
        INJECTION_ANNOTATIONS.add("jakarta.annotation.Resource");
        INJECTION_ANNOTATIONS.add("org.springframework.beans.factory.annotation.Autowired");
        INJECTION_ANNOTATIONS.add("org.springframework.beans.factory.annotation.Qualifier");
        INJECTION_ANNOTATIONS.add("javax.inject.Inject");
        INJECTION_ANNOTATIONS.add("jakarta.inject.Inject");
    }
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

    /**
     * Check if the reference expression is an injected field of the current class
     *
     * @param referenceExpression the reference expression
     * @param psiFile current file
     * @return true if it is an injected field, false otherwise
     */
    public static boolean isInjectedField(@NotNull PsiReferenceExpression referenceExpression, @NotNull PsiFile psiFile) {
        String referenceName = referenceExpression.getReferenceName();
        if (referenceName == null) {
            return false;
        }

        // Check if this reference has a qualifier (e.g., already has a prefix)
        if (referenceExpression.getQualifierExpression() != null) {
            return false;
        }

        PsiClass containingClass = PsiTreeUtil.getParentOfType(referenceExpression, PsiClass.class);
        if (containingClass == null) {
            return false;
        }

        // Find the field in the containing class
        PsiField field = containingClass.findFieldByName(referenceName, false);
        if (field == null) {
            return false;
        }

        // Check if the field has any injection annotation
        return hasInjectionAnnotation(field);
    }

    /**
     * Check if a field has any injection annotation
     *
     * @param field the field to check
     * @return true if the field has an injection annotation, false otherwise
     */
    private static boolean hasInjectionAnnotation(@NotNull PsiField field) {
        for (PsiAnnotation annotation : field.getAnnotations()) {
            String qualifiedName = annotation.getQualifiedName();
            if (qualifiedName != null && INJECTION_ANNOTATIONS.contains(qualifiedName)) {
                return true;
            }
        }
        return false;
    }
}