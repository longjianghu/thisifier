package com.sohocn.thisifier.actions;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.CommonDataKeys;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.command.WriteCommandAction;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.project.Project;
import com.intellij.psi.*;
import com.intellij.psi.util.PsiTreeUtil;
import com.sohocn.thisifier.util.MethodDetectionUtil;
import com.sohocn.thisifier.util.ErrorHandlingUtil;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

/**
 * Main action class for adding 'this' to method calls
 */
public class AddThisAction extends AnAction {
    
    @Override
    public void update(@NotNull AnActionEvent e) {
        // Get the current editor and project
        Editor editor = e.getData(CommonDataKeys.EDITOR);
        Project project = e.getData(CommonDataKeys.PROJECT);
        
        if (editor == null || project == null) {
            e.getPresentation().setEnabledAndVisible(false);
            return;
        }
        
        // Get the current caret position
        PsiFile psiFile = e.getData(CommonDataKeys.PSI_FILE);
        if (!(psiFile instanceof PsiJavaFile)) {
            e.getPresentation().setEnabledAndVisible(false);
            return;
        }
        
        // Check if we have eligible method calls at the current position
        boolean hasEligibleMethodCalls = hasEligibleMethodCalls(editor, (PsiJavaFile) psiFile);
        
        // Update the presentation based on eligibility
        e.getPresentation().setEnabledAndVisible(hasEligibleMethodCalls);
    }
    
    @Override
    public void actionPerformed(@NotNull AnActionEvent e) {
        Editor editor = e.getData(CommonDataKeys.EDITOR);
        Project project = e.getData(CommonDataKeys.PROJECT);
        PsiFile psiFile = e.getData(CommonDataKeys.PSI_FILE);
        
        if (editor == null || project == null || !(psiFile instanceof PsiJavaFile)) {
            return;
        }
        
        // Get all eligible method calls in the current selection or at caret
        List<PsiMethodCallExpression> methodCalls = getEligibleMethodCalls(editor, (PsiJavaFile) psiFile);
        
        if (methodCalls.isEmpty()) {
            return;
        }
        
        // Perform the modification in a write action
        WriteCommandAction.runWriteCommandAction(project, () -> {
            for (PsiMethodCallExpression methodCall : methodCalls) {
                addThisToMethodCall(methodCall);
            }
        });
    }
    
    /**
     * Check if there are eligible method calls at the current position
     */
    private boolean hasEligibleMethodCalls(Editor editor, PsiJavaFile psiFile) {
        List<PsiMethodCallExpression> methodCalls = getEligibleMethodCalls(editor, psiFile);
        return !methodCalls.isEmpty();
    }
    
    /**
     * Get all eligible method calls in the current selection or at caret
     */
    private List<PsiMethodCallExpression> getEligibleMethodCalls(Editor editor, PsiJavaFile psiFile) {
        List<PsiMethodCallExpression> eligibleMethodCalls = new ArrayList<>();
        
        // Get the caret offset
        int caretOffset = editor.getCaretModel().getOffset();
        
        // Find the element at caret
        PsiElement element = psiFile.findElementAt(caretOffset);
        if (element == null) {
            return eligibleMethodCalls;
        }
        
        // Check if the element is a method call or part of one
        PsiMethodCallExpression methodCall = PsiTreeUtil.getParentOfType(element, PsiMethodCallExpression.class);
        if (methodCall != null && ErrorHandlingUtil.isValidMethodCall(methodCall) && 
            ErrorHandlingUtil.safeIsEligibleForThisAddition(methodCall)) {
            ErrorHandlingUtil.logOperation("Found eligible method call", methodCall);
            eligibleMethodCalls.add(methodCall);
        }
        
        // Check for multiple selections
        if (editor.getSelectionModel().hasSelection()) {
            int selectionStart = editor.getSelectionModel().getSelectionStart();
            int selectionEnd = editor.getSelectionModel().getSelectionEnd();
            
            // Find all method calls in the selection range
            PsiElement[] elements = PsiTreeUtil.collectElements(psiFile, 
                el -> el instanceof PsiMethodCallExpression && 
                       el.getTextRange().getStartOffset() >= selectionStart && 
                       el.getTextRange().getEndOffset() <= selectionEnd);
            
            for (PsiElement el : elements) {
                if (el instanceof PsiMethodCallExpression call && 
                    ErrorHandlingUtil.isValidMethodCall(call) && 
                    ErrorHandlingUtil.safeIsEligibleForThisAddition(call)) {
                    ErrorHandlingUtil.logOperation("Found eligible method call in selection", call);
                    eligibleMethodCalls.add(call);
                }
            }
        }
        
        return eligibleMethodCalls;
    }
    
    /**
     * Add 'this.' prefix to a method call
     */
    private void addThisToMethodCall(PsiMethodCallExpression methodCall) {
        if (!ErrorHandlingUtil.isValidMethodCall(methodCall)) {
            ErrorHandlingUtil.logOperation("Skipping invalid method call", methodCall);
            return;
        }
        
        try {
            // Get the method expression
            PsiReferenceExpression methodExpression = methodCall.getMethodExpression();
            
            // Get the method name safely
            String methodName = ErrorHandlingUtil.safeGetMethodName(methodCall);
            if (methodName == null) {
                ErrorHandlingUtil.logOperation("Failed to get method name", methodCall);
                return;
            }
            
            // Create a new 'this' qualifier
            PsiElementFactory factory = JavaPsiFacade.getElementFactory(methodCall.getProject());
            
            // Create a new qualified expression
            PsiExpression qualifiedExpression = factory.createExpressionFromText(
                "this." + methodName, methodCall);
            
            // Replace the method expression with the qualified one
            methodExpression.replace(qualifiedExpression);
            
            ErrorHandlingUtil.logOperation("Successfully added 'this' to method call", methodCall);
            
        } catch (Exception e) {
            ErrorHandlingUtil.logOperation("Error adding 'this' to method call", methodCall);
            // Re-throw the exception to be handled by the write command action
            throw e;
        }
    }
}