package com.sohocn.thisifier.actions;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.CommonDataKeys;
import com.intellij.openapi.actionSystem.ActionUpdateThread;
import com.intellij.openapi.command.WriteCommandAction;
import com.intellij.openapi.editor.Editor;
import com.intellij.psi.*;
import com.intellij.psi.util.PsiTreeUtil;
import com.sohocn.thisifier.util.MethodDetectionUtil;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;

/**
 * The type Add this action.
 *
 * @author longjianghu
 */
public class AddThisAction extends AnAction {
    @Override
    public ActionUpdateThread getActionUpdateThread() {
        return ActionUpdateThread.BGT;
    }
    
    @Override
    public void actionPerformed(@NotNull AnActionEvent e) {
        PsiFile psiFile = e.getData(CommonDataKeys.PSI_FILE);
        Editor editor = e.getData(CommonDataKeys.EDITOR);

        if (psiFile == null || editor == null) {
            return;
        }

        WriteCommandAction.runWriteCommandAction(psiFile.getProject(), () -> {
            // Process method calls first
            Collection<PsiMethodCallExpression> methodCallsCollection = PsiTreeUtil.findChildrenOfType(psiFile, PsiMethodCallExpression.class);
            for (PsiMethodCallExpression methodCall : methodCallsCollection) {
                if (MethodDetectionUtil.isCurrentClassInstanceMethod(methodCall, psiFile)) {
                    addThisPrefix(methodCall);
                }
            }

            // Process field references (including injected fields)
            // Collect references first to avoid concurrent modification issues
            Collection<PsiReferenceExpression> referenceExpressions = PsiTreeUtil.findChildrenOfType(psiFile, PsiReferenceExpression.class);
            for (PsiReferenceExpression referenceExpression : referenceExpressions) {
                // Skip if it's a method call
                if (referenceExpression instanceof PsiMethodCallExpression) {
                    continue;
                }
                // Add this. prefix for injected fields
                if (MethodDetectionUtil.isInjectedField(referenceExpression, psiFile)) {
                    addThisPrefix(referenceExpression);
                }
            }
        });
    }

    @Override
    public void update(@NotNull AnActionEvent e) {
        e.getPresentation().setVisible(true);
        e.getPresentation().setEnabled(false);

        PsiFile psiFile = e.getData(CommonDataKeys.PSI_FILE);
        Editor editor = e.getData(CommonDataKeys.EDITOR);

        if (psiFile instanceof PsiJavaFile && editor != null) {
            boolean hasValidMethodCall = hasValidMethodCallInFile((PsiJavaFile) psiFile);
            boolean hasInjectedField = hasInjectedFieldReferenceInFile((PsiJavaFile) psiFile);
            e.getPresentation().setEnabled(hasValidMethodCall || hasInjectedField);
        }
    }
    
    /**
     * Check if there's at least one method call in the file that satisfies the condition
     *
     * @param javaFile the Java file to check
     * @return true if there's at least one valid method call, false otherwise
     */
    private boolean hasValidMethodCallInFile(PsiJavaFile javaFile) {
        Collection<PsiMethodCallExpression> methodCalls = PsiTreeUtil.findChildrenOfType(javaFile, PsiMethodCallExpression.class);

        for (PsiMethodCallExpression methodCall : methodCalls) {
            if (methodCall != null && MethodDetectionUtil.isCurrentClassInstanceMethod(methodCall, javaFile)) {
                return true;
            }
        }

        return false;
    }

    /**
     * Check if there's at least one injected field reference in the file
     *
     * @param javaFile the Java file to check
     * @return true if there's at least one injected field reference, false otherwise
     */
    private boolean hasInjectedFieldReferenceInFile(PsiJavaFile javaFile) {
        Collection<PsiReferenceExpression> references = PsiTreeUtil.findChildrenOfType(javaFile, PsiReferenceExpression.class);

        for (PsiReferenceExpression reference : references) {
            if (reference instanceof PsiMethodCallExpression) {
                continue;
            }
            if (reference != null && MethodDetectionUtil.isInjectedField(reference, javaFile)) {
                return true;
            }
        }

        return false;
    }

    private void addThisPrefix(PsiMethodCallExpression methodCall) {
        PsiReferenceExpression methodExpression = methodCall.getMethodExpression();

        if (methodExpression.getQualifierExpression() != null) {
            return;
        }

        PsiElementFactory factory = JavaPsiFacade.getElementFactory(methodCall.getProject());
        PsiReferenceExpression newMethodExpression = (PsiReferenceExpression) factory.createExpressionFromText(
                "this." + methodExpression.getText(), methodCall);

        methodExpression.replace(newMethodExpression);
    }

    private void addThisPrefix(PsiReferenceExpression referenceExpression) {
        if (referenceExpression.getQualifierExpression() != null) {
            return;
        }

        PsiElementFactory factory = JavaPsiFacade.getElementFactory(referenceExpression.getProject());
        PsiReferenceExpression newReferenceExpression = (PsiReferenceExpression) factory.createExpressionFromText(
                "this." + referenceExpression.getText(), referenceExpression);

        referenceExpression.replace(newReferenceExpression);
    }
}