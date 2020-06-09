/*******************************************************************************
 * Copyright (c) 2000, 2007 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org2.eclipse.php.internal.debug.ui.watch;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.debug.core.DebugEvent;
import org.eclipse.debug.core.DebugException;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.model.IDebugElement;
import org.eclipse.debug.core.model.IDebugTarget;
import org.eclipse.debug.core.model.IStackFrame;
import org.eclipse.debug.core.model.IThread;
import org.eclipse.debug.core.model.IValue;
import org.eclipse.debug.core.model.IWatchExpressionDelegate;
import org.eclipse.debug.core.model.IWatchExpressionListener;
import org.eclipse.debug.core.model.IWatchExpressionResult;
import org2.eclipse.php.internal.debug.core.zend.debugger.DefaultExpressionsManager;
import org2.eclipse.php.internal.debug.core.zend.debugger.Expression;
import org2.eclipse.php.internal.debug.core.zend.model.PHPDebugTarget;
import org2.eclipse.php.internal.debug.core.zend.model.PHPValue;

import com.aptana.php.debug.epl.PHPDebugEPLPlugin;

/**
 * 
 */
public class PHPWatchExpressionDelegate implements IWatchExpressionDelegate {

    private String fExpressionText;
    private IWatchExpressionListener fListener;
    private PHPDebugTarget debugTarget;
    private Job fRunDispatch;

    /**
     * @see org.eclipse.debug.core.model.IWatchExpressionDelegate#getValue(java.lang.String, org.eclipse.debug.core.model.IDebugElement)
     */
    public void evaluateExpression(String expression, IDebugElement context, IWatchExpressionListener listener) {
        fExpressionText = expression;
        fListener = listener;
        // find a stack frame context if possible.
        IStackFrame frame = null;
        if (context instanceof IStackFrame) {
            frame = (IStackFrame) context;
        } else if (context instanceof IThread) {
            try {
                frame = ((IThread) context).getTopStackFrame();
            } catch (DebugException e) {
                PHPDebugEPLPlugin.logError(e);
            }
        }
        if (frame == null) {
            fListener.watchEvaluationFinished(null);
        } else {
            IDebugTarget target = frame.getDebugTarget();
            if (target instanceof PHPDebugTarget){
                debugTarget = (PHPDebugTarget) target;
                fRunDispatch = new EvaluationRunnable();
                fRunDispatch.schedule();
            } else {
                fListener.watchEvaluationFinished(null);
            }
        }
    }

    /**
     * Runnable used to evaluate the expression.
     */
    private final class EvaluationRunnable extends Job {

        public EvaluationRunnable() {
            super("EvaluationRunnable");
            setSystem(true);
        }

        public IStatus run(IProgressMonitor monitor) {

            try {
                IWatchExpressionResult watchResult = new IWatchExpressionResult() {
                    public IValue getValue() {
                        Expression value = getExpression(debugTarget, fExpressionText);
                        IValue iValue = new PHPValue(debugTarget, value);
                        return iValue;
                    }

                    public boolean hasErrors() {
                        return false;
                    }

                    public String[] getErrorMessages() {
                        return null;
                    }

                    public String getExpressionText() {
                        return fExpressionText;
                    }

                    public DebugException getException() {
                        return null;
                    }
                };
                fListener.watchEvaluationFinished(watchResult);
            } catch (Exception e) {
                PHPDebugEPLPlugin.logError(e);
                fListener.watchEvaluationFinished(null);
                // TODo fix
            }
            DebugPlugin.getDefault().fireDebugEventSet(new DebugEvent[] { new DebugEvent(PHPWatchExpressionDelegate.this, DebugEvent.SUSPEND, DebugEvent.EVALUATION_IMPLICIT) });
            return Status.OK_STATUS;
        }
    }

    /**
     * Returns the variable value.
     * 
     * @param variable The variable name
     * @return
     */
    protected Expression getExpression(PHPDebugTarget debugTarget, String variable) {
        DefaultExpressionsManager expressionManager = debugTarget.getExpressionManager();
        Expression expression = expressionManager.buildExpression(variable);

        // Get the value from the debugger
        debugTarget.getExpressionManager().getExpressionValue(expression, 1);
        expressionManager.update(expression, 1);
        return expression;
    }
}
