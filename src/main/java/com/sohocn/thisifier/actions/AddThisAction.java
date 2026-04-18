package com.sohocn.thisifier.actions;

import java.util.Collection;

import org.jetbrains.annotations.NotNull;

import com.intellij.openapi.actionSystem.ActionUpdateThread;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.CommonDataKeys;
import com.intellij.openapi.command.WriteCommandAction;
import com.intellij.openapi.editor.Editor;
import com.intellij.psi.*;
import com.intellij.psi.util.PsiTreeUtil;
import com.sohocn.thisifier.util.MethodDetectionUtil;

/**
 * The type Add this action.
 *
 * @author longjianghu
 */
public class AddThisAction extends AnAction {
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
                    this.addThisPrefix(methodCall);
                }
            }

            Collection<PsiReferenceExpression> referenceExpressions = PsiTreeUtil.findChildrenOfType(psiFile, PsiReferenceExpression.class);
            for (PsiReferenceExpression referenceExpression : referenceExpressions) {
                if (referenceExpression instanceof PsiMethodCallExpression) {
                    continue;
                }

                if (MethodDetectionUtil.isInjectedField(referenceExpression, psiFile)) {
                    this.addThisPrefix(referenceExpression);
                }
            }
        });
    }

    @Override
    public @NotNull ActionUpdateThread getActionUpdateThread() {
        return ActionUpdateThread.BGT;
    }

    @Override
    public void update(@NotNull AnActionEvent e) {
        e.getPresentation().setVisible(true);
        e.getPresentation().setEnabled(false);

        PsiFile psiFile = e.getData(CommonDataKeys.PSI_FILE);
        Editor editor = e.getData(CommonDataKeys.EDITOR);

        if (psiFile instanceof PsiJavaFile && editor != null) {
            boolean hasValidMethodCall = this.hasValidMethodCallInFile((PsiJavaFile)psiFile);
            boolean hasInjectedField = this.hasInjectedFieldReferenceInFile((PsiJavaFile)psiFile);
            e.getPresentation().setEnabled(hasValidMethodCall || hasInjectedField);
        }
    }

    private void addThisPrefix(PsiMethodCallExpression methodCall) {
        PsiReferenceExpression methodExpression = methodCall.getMethodExpression();

        if (methodExpression.getText().startsWith("this.")) {
            return;
        }

        this.addThisPrefix(methodExpression);
    }

    private void addThisPrefix(PsiReferenceExpression referenceExpression) {
        if (referenceExpression.getQualifierExpression() != null) {
            return;
        }

        if (referenceExpression.getText().startsWith("this.")) {
            return;
        }

        PsiElementFactory factory = JavaPsiFacade.getElementFactory(referenceExpression.getProject());
        PsiReferenceExpression newReferenceExpression = (PsiReferenceExpression) factory.createExpressionFromText(
                "this." + referenceExpression.getText(), referenceExpression);

        referenceExpression.replace(newReferenceExpression);
    }

    private boolean hasInjectedFieldReferenceInFile(PsiJavaFile javaFile) {
        Collection<PsiReferenceExpression> references =
            PsiTreeUtil.findChildrenOfType(javaFile, PsiReferenceExpression.class);

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

    private boolean hasValidMethodCallInFile(PsiJavaFile javaFile) {
        Collection<PsiMethodCallExpression> methodCalls =
            PsiTreeUtil.findChildrenOfType(javaFile, PsiMethodCallExpression.class);

        for (PsiMethodCallExpression methodCall : methodCalls) {
            if (methodCall != null && MethodDetectionUtil.isCurrentClassInstanceMethod(methodCall, javaFile)) {
                return true;
            }
        }

        return false;
    }
}