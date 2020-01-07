package model;

        import com.github.javaparser.ast.NodeList;
        import com.github.javaparser.ast.body.MethodDeclaration;
        import com.github.javaparser.ast.stmt.ForStmt;
        import com.github.javaparser.ast.stmt.IfStmt;
        import com.github.javaparser.ast.stmt.ReturnStmt;
        import com.github.javaparser.ast.stmt.SwitchEntry;
        import com.github.javaparser.ast.stmt.SwitchStmt;
        import com.github.javaparser.ast.stmt.WhileStmt;

        import java.util.List;

public class CyclomaticComplexityCalculator {
    private int e;
    private int n;
    private int p;
    private MethodDeclaration method;

    public CyclomaticComplexityCalculator() {
        this.p = 1;
    }

    public int computeComplexityForMethod(MethodDeclaration method) {
        this.method = method;
        calculateIfStmts();
        calculateSwitchStmts();
        calculateForStmts();
        calculateWhileStmts();
        calculateReturnStmts();
        return e - n + p;
    }


    private void calculateIfStmts() {
        List<IfStmt> ifStmts = method.getChildNodesByType(IfStmt.class);
        e += ifStmts.size() * 2;
        n += ifStmts.size();
    }

    private void calculateSwitchStmts() {
        List<SwitchStmt> switchStmts = method.getChildNodesByType(SwitchStmt.class);
        for (SwitchStmt switchStmt : switchStmts) {
            NodeList<SwitchEntry> cases = switchStmt.getEntries();
            e += cases.size();
            n ++;
        }
    }

    private void calculateForStmts() {
        List<ForStmt> forStmts = method.getChildNodesByType(ForStmt.class);
        e += forStmts.size() * 2;
        n += forStmts.size();
    }

    private void calculateWhileStmts() {
        List<WhileStmt> whileStmts = method.getChildNodesByType(WhileStmt.class);
        e += whileStmts.size() * 2;
        n += whileStmts.size();
    }

    private void calculateReturnStmts() {
        List<ReturnStmt> returnStmts = method.getChildNodesByType(ReturnStmt.class);
        p += returnStmts.size();
        n += returnStmts.size();
    }
}