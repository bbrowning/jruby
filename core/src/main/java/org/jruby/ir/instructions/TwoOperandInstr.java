package org.jruby.ir.instructions;

import org.jruby.ir.Operation;
import org.jruby.ir.operands.Operand;

/**
 * Instr for two operands.
 */
public abstract class TwoOperandInstr extends Instr {
    private Operand operand1;
    private Operand operand2;

    public TwoOperandInstr(Operation operation, Operand operand1, Operand operand2) {
        super(operation);

        this.operand1 = operand1;
        this.operand2 = operand2;
    }

    public Operand[] getOperands() {
        return new Operand[] { operand1, operand2 };
    }

    public Operand getOperand1() {
        return operand1;
    }

    public Operand getOperand2() {
        return operand2;
    }

    public void setOperand1(Operand operand1) {
        this.operand1 = operand1;
    }

    public void setOperand2(Operand operand2) {
        this.operand2 = operand2;
    }

    public void setOperand(int i, Operand operand) {
        switch (i) {
            case 0:
                operand1 = operand;
                break;
            case 1:
                operand2 = operand;
                break;
            default:
                throw new IllegalArgumentException("No such operand to set at index: " + i);
        }
    }

}
