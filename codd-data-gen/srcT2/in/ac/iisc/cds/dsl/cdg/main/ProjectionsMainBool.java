package in.ac.iisc.cds.dsl.cdg.main;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.Set;

import com.microsoft.z3.ArithExpr;
import com.microsoft.z3.BoolExpr;
import com.microsoft.z3.Context;
import com.microsoft.z3.Model;
import com.microsoft.z3.Solver;
import com.microsoft.z3.Status;

public class ProjectionsMainBool {

    public int        a, b, c, ab, ac, bc, abc;
    public Reltuple[] rel;
    int               varcount;
    int               ntccount;

    public ProjectionsMainBool(int a, int b, int c, int ab, int ac, int bc, int abc) {
        this.a = a;
        this.b = b;
        this.c = c;
        this.ab = ab;
        this.ac = ac;
        this.bc = bc;
        this.abc = abc;

        rel = new Reltuple[this.abc];

        varcount = 0;
        ntccount = 0;
    }

    public boolean isInputValid() {

        if (!(a < b && b < c && c < ab && ab < ac && ac < bc && bc < abc))
            return false;

        if (!(ab <= a * b && ac <= a * c && bc <= b * c))
            return false;

        if (!(abc <= ab * c && abc <= ac * b && abc <= bc * a))
            return false;

        return true;
    }

    public void solve() {

        Map<String, String> contextmap = new HashMap<>();
        contextmap.put("model", "true");
        contextmap.put("unsat_core", "true");
        Context ctx = new Context(contextmap);

        Solver solver = ctx.mkSolver();

        long formulationstart = System.currentTimeMillis();

        //abc
        addABCcons(ctx, solver);

        //ab
        addABcons(ctx, solver);

        //ac
        addACcons(ctx, solver);

        //bc
        addBCcons(ctx, solver);

        //a
        addAcons(ctx, solver);

        //b
        addBcons(ctx, solver);

        //c
        addCcons(ctx, solver);

        long solvestart = System.currentTimeMillis();

        System.out.println("Solving with " + varcount + " variables and " + ntccount + " non-trivial constraints");

        Status solverStatus = solver.check();
        System.out.println("Solver Status: " + solverStatus);

        if (!Status.SATISFIABLE.equals(solverStatus))
            throw new RuntimeException("solverStatus is not SATISFIABLE");

        Model model = solver.getModel();

        //        for (int i = 0; i < a; i++) {
        //            for (int j = 0; j < b; j++) {
        //                String varname = "yab_" + i + "_" + j;
        //                int value = Integer.parseInt(model.evaluate(ctx.mkIntConst(varname), true).toString());
        //                System.out.println("varname=" + value);
        //            }
        //        }

        int p = -1;
        for (int i = 0; i < a; i++) {
            for (int j = 0; j < b; j++) {
                for (int k = 0; k < c; k++) {
                    String varname = "x_" + i + "_" + j + "_" + k;
                    boolean value = Boolean.parseBoolean(model.evaluate(ctx.mkBoolConst(varname), true).toString());
                    if (value) {
                        rel[++p] = new Reltuple(varname);
                    }
                }
            }
        }
        if (p != abc - 1)
            throw new RuntimeException("Found p=" + p + " while expected to be abc-1=" + (abc - 1));

        long solveend = System.currentTimeMillis();

        System.out.println("\tFormulation:\t" + (solvestart - formulationstart) + " ms");
        System.out.println("\tSolving:\t" + (solveend - solvestart) + " ms");
    }

    private void addABCcons(Context ctx, Solver solver) {
        List<ArithExpr> xabcAddList = new ArrayList<>(a * b * c);
        for (int i = 0; i < a; i++) {
            for (int j = 0; j < b; j++) {
                for (int k = 0; k < c; k++) {
                    String varname = "x_" + i + "_" + j + "_" + k;
                    varcount++;
                    xabcAddList.add((ArithExpr) ctx.mkITE(ctx.mkBoolConst(varname), ctx.mkInt(1), ctx.mkInt(0)));
                }
            }
        }
        solver.add(ctx.mkEq(ctx.mkAdd(xabcAddList.toArray(new ArithExpr[xabcAddList.size()])), ctx.mkInt(abc)));
        ntccount++;
    }

    private void addABcons(Context ctx, Solver solver) {
        List<ArithExpr> yabAddList = new ArrayList<>(a * b);
        for (int i = 0; i < a; i++) {
            for (int j = 0; j < b; j++) {
                String varname = "yab_" + i + "_" + j;
                varcount++;
                yabAddList.add((ArithExpr) ctx.mkITE(ctx.mkBoolConst(varname), ctx.mkInt(1), ctx.mkInt(0)));

                List<BoolExpr> addList = new ArrayList<>(c);
                for (int k = 0; k < c; k++) {
                    String temp = "x_" + i + "_" + j + "_" + k;
                    addList.add(ctx.mkBoolConst(temp));
                }
                solver.add(ctx.mkEq(ctx.mkBoolConst(varname), ctx.mkOr(addList.toArray(new BoolExpr[addList.size()]))));
                ntccount++;
            }
        }
        solver.add(ctx.mkEq(ctx.mkAdd(yabAddList.toArray(new ArithExpr[yabAddList.size()])), ctx.mkInt(ab)));
        ntccount++;
    }

    private void addACcons(Context ctx, Solver solver) {
        List<ArithExpr> yacAddList = new ArrayList<>(a * c);
        for (int i = 0; i < a; i++) {
            for (int k = 0; k < c; k++) {
                String varname = "yac_" + i + "_" + k;
                varcount++;
                yacAddList.add((ArithExpr) ctx.mkITE(ctx.mkBoolConst(varname), ctx.mkInt(1), ctx.mkInt(0)));

                List<BoolExpr> addList = new ArrayList<>(b);
                for (int j = 0; j < b; j++) {
                    String temp = "x_" + i + "_" + j + "_" + k;
                    addList.add(ctx.mkBoolConst(temp));
                }
                solver.add(ctx.mkEq(ctx.mkBoolConst(varname), ctx.mkOr(addList.toArray(new BoolExpr[addList.size()]))));
                ntccount++;
            }
        }
        solver.add(ctx.mkEq(ctx.mkAdd(yacAddList.toArray(new ArithExpr[yacAddList.size()])), ctx.mkInt(ac)));
        ntccount++;
    }

    private void addBCcons(Context ctx, Solver solver) {
        List<ArithExpr> ybcAddList = new ArrayList<>(b * c);
        for (int j = 0; j < b; j++) {
            for (int k = 0; k < c; k++) {
                String varname = "ybc_" + j + "_" + k;
                varcount++;
                ybcAddList.add((ArithExpr) ctx.mkITE(ctx.mkBoolConst(varname), ctx.mkInt(1), ctx.mkInt(0)));

                List<BoolExpr> addList = new ArrayList<>(a);
                for (int i = 0; i < a; i++) {
                    String temp = "x_" + i + "_" + j + "_" + k;
                    addList.add(ctx.mkBoolConst(temp));
                }
                solver.add(ctx.mkEq(ctx.mkBoolConst(varname), ctx.mkOr(addList.toArray(new BoolExpr[addList.size()]))));
                ntccount++;
            }
        }
        solver.add(ctx.mkEq(ctx.mkAdd(ybcAddList.toArray(new ArithExpr[ybcAddList.size()])), ctx.mkInt(bc)));
        ntccount++;
    }

    private void addAcons(Context ctx, Solver solver) {
        List<ArithExpr> yaAddList = new ArrayList<>(a);
        for (int i = 0; i < a; i++) {
            String varname = "ya_" + i;
            varcount++;
            yaAddList.add((ArithExpr) ctx.mkITE(ctx.mkBoolConst(varname), ctx.mkInt(1), ctx.mkInt(0)));

            List<BoolExpr> addList = new ArrayList<>(b * c);
            for (int j = 0; j < b; j++) {
                for (int k = 0; k < c; k++) {
                    String temp = "x_" + i + "_" + j + "_" + k;
                    addList.add(ctx.mkBoolConst(temp));
                }
            }
            solver.add(ctx.mkEq(ctx.mkBoolConst(varname), ctx.mkOr(addList.toArray(new BoolExpr[addList.size()]))));
            ntccount++;
        }
        solver.add(ctx.mkEq(ctx.mkAdd(yaAddList.toArray(new ArithExpr[yaAddList.size()])), ctx.mkInt(a)));
        ntccount++;
    }

    private void addBcons(Context ctx, Solver solver) {
        List<ArithExpr> ybAddList = new ArrayList<>(b);
        for (int j = 0; j < b; j++) {
            String varname = "yb_" + j;
            varcount++;
            ybAddList.add((ArithExpr) ctx.mkITE(ctx.mkBoolConst(varname), ctx.mkInt(1), ctx.mkInt(0)));

            List<BoolExpr> addList = new ArrayList<>(a * c);
            for (int i = 0; i < a; i++) {
                for (int k = 0; k < c; k++) {
                    String temp = "x_" + i + "_" + j + "_" + k;
                    addList.add(ctx.mkBoolConst(temp));
                }
            }
            solver.add(ctx.mkEq(ctx.mkBoolConst(varname), ctx.mkOr(addList.toArray(new BoolExpr[addList.size()]))));
            ntccount++;
        }
        solver.add(ctx.mkEq(ctx.mkAdd(ybAddList.toArray(new ArithExpr[ybAddList.size()])), ctx.mkInt(b)));
        ntccount++;
    }

    private void addCcons(Context ctx, Solver solver) {
        List<ArithExpr> ycAddList = new ArrayList<>(c);
        for (int k = 0; k < c; k++) {
            String varname = "yc_" + k;
            varcount++;
            ycAddList.add((ArithExpr) ctx.mkITE(ctx.mkBoolConst(varname), ctx.mkInt(1), ctx.mkInt(0)));

            List<BoolExpr> addList = new ArrayList<>(a * b);
            for (int i = 0; i < a; i++) {
                for (int j = 0; j < b; j++) {
                    String temp = "x_" + i + "_" + j + "_" + k;
                    addList.add(ctx.mkBoolConst(temp));
                }
            }
            solver.add(ctx.mkEq(ctx.mkBoolConst(varname), ctx.mkOr(addList.toArray(new BoolExpr[addList.size()]))));
            ntccount++;
        }
        solver.add(ctx.mkEq(ctx.mkAdd(ycAddList.toArray(new ArithExpr[ycAddList.size()])), ctx.mkInt(c)));
        ntccount++;
    }

    void displayRel(int start, int upto) {
        System.out.println("----------------------------------");
        System.out.println("      #      A      B      C");
        System.out.println("----------------------------------");
        for (int i = start; i < upto; i++) {
            System.out.printf("    %3d    %3d    %3d    %3d\n", i, rel[i].aval, rel[i].bval, rel[i].cval);
        }
        System.out.println("----------------------------------");
    }

    void validateOutput(int upto) {
        Set<Integer> aset = new HashSet<>(), bset = new HashSet<>(), cset = new HashSet<>();
        Set<Intpair> abset = new HashSet<>(), acset = new HashSet<>(), bcset = new HashSet<>();
        Set<Reltuple> abcset = new HashSet<>();

        for (int i = 0; i < upto; i++) {
            aset.add(rel[i].aval);
            bset.add(rel[i].bval);
            cset.add(rel[i].cval);

            Intpair abpair = new Intpair(rel[i].aval, rel[i].bval);
            abset.add(abpair);

            Intpair acpair = new Intpair(rel[i].aval, rel[i].cval);
            acset.add(acpair);

            Intpair bcpair = new Intpair(rel[i].bval, rel[i].cval);
            bcset.add(bcpair);

            Reltuple abctrip = rel[i];
            abcset.add(abctrip);
        }

        System.out.print(a == aset.size() ? "TRUE" : "FALSE");
        System.out.printf("\tneeded   a=%3d found=%3d\n", a, aset.size());
        System.out.print(b == bset.size() ? "TRUE" : "FALSE");
        System.out.printf("\tneeded   b=%3d found=%3d\n", b, bset.size());
        System.out.print(c == cset.size() ? "TRUE" : "FALSE");
        System.out.printf("\tneeded   c=%3d found=%3d\n", c, cset.size());
        System.out.print(ab == abset.size() ? "TRUE" : "FALSE");
        System.out.printf("\tneeded  ab=%3d found=%3d\n", ab, abset.size());
        System.out.print(ac == acset.size() ? "TRUE" : "FALSE");
        System.out.printf("\tneeded  ac=%3d found=%3d\n", ac, acset.size());
        System.out.print(bc == bcset.size() ? "TRUE" : "FALSE");
        System.out.printf("\tneeded  bc=%3d found=%3d\n", bc, bcset.size());
        System.out.print(abc == abcset.size() ? "TRUE" : "FALSE");
        System.out.printf("\tneeded abc=%3d found=%3d\n", abc, abcset.size());
        System.out.println();
    }

    public static void main(String[] args) {

        int a, b, c, ab, ac, bc, abc;
        Scanner scanner = new Scanner(System.in);

        ProjectionsMainBool obj;
        do {
            System.out.print("Enter |A|, |B|, |C|, |AB|, |AC|, |BC|, |ABC|: ");
            a = scanner.nextInt();
            b = scanner.nextInt();
            c = scanner.nextInt();
            ab = scanner.nextInt();
            ac = scanner.nextInt();
            bc = scanner.nextInt();
            abc = scanner.nextInt();
            obj = new ProjectionsMainBool(a, b, c, ab, ac, bc, abc);
        } while (!obj.isInputValid());

        scanner.close();

        obj.solve();

        obj.displayRel(0, abc);
        obj.validateOutput(obj.abc);
    }
}
