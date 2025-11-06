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
        
        // Find all method calls in the file and add 'this.' prefix to valid ones
        Collection<PsiMethodCallExpression> methodCallsCollection = PsiTreeUtil.findChildrenOfType(psiFile, PsiMethodCallExpression.class);
        
        if (!methodCallsCollection.isEmpty()) {
            WriteCommandAction.runWriteCommandAction(psiFile.getProject(), () -> {
                for (PsiMethodCallExpression methodCall : methodCallsCollection) {
                    if (MethodDetectionUtil.isCurrentClassInstanceMethod(methodCall, psiFile)) {
                        addThisPrefix(methodCall);
                    }
                }
            });
        }
    }

    @Override
    public void update(@NotNull AnActionEvent e) {
        // Always show the menu item
        e.getPresentation().setVisible(true);
        
        // But disable it by default
        e.getPresentation().setEnabled(false);
        
        PsiFile psiFile = e.getData(CommonDataKeys.PSI_FILE);
        Editor editor = e.getData(CommonDataKeys.EDITOR);
        
        if (psiFile instanceof PsiJavaFile && editor != null) {
            // Check if there's at least one method call in the file that satisfies the condition
            boolean hasValidMethodCall = hasValidMethodCallInFile((PsiJavaFile) psiFile);
            e.getPresentation().setEnabled(hasValidMethodCall);
        }
    }
    
    /**
     * Check if there's at least one method call in the file that satisfies the condition
     * 
     * @param javaFile the Java file to check
     * @return true if there's at least one valid method call, false otherwise
     */
    private boolean hasValidMethodCallInFile(PsiJavaFile javaFile) {
        // Find all method call expressions in the file
        Collection<PsiMethodCallExpression> methodCalls = PsiTreeUtil.findChildrenOfType(javaFile, PsiMethodCallExpression.class);
        
        // Check each method call
        if (methodCalls != null) {
            for (PsiMethodCallExpression methodCall : methodCalls) {
                if (methodCall != null && MethodDetectionUtil.isCurrentClassInstanceMethod(methodCall, javaFile)) {
                    return true;
                }
            }
        }
        
        return false;
    }

    private void addThisPrefix(PsiMethodCallExpression methodCall) {
        PsiReferenceExpression methodExpression = methodCall.getMethodExpression();
        
        // Check if 'this.' already exists
        if (methodExpression.getQualifierExpression() != null) {
            return;
        }
        
        PsiElementFactory factory = JavaPsiFacade.getElementFactory(methodCall.getProject());
        PsiReferenceExpression newMethodExpression = (PsiReferenceExpression) factory.createExpressionFromText(
                "this." + methodExpression.getText(), methodCall);
        
        methodExpression.replace(newMethodExpression);
    }
}