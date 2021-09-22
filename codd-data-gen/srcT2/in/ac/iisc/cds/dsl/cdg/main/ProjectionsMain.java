package in.ac.iisc.cds.dsl.cdg.main;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.Set;

import com.microsoft.z3.ArithExpr;
import com.microsoft.z3.Context;
import com.microsoft.z3.IntExpr;
import com.microsoft.z3.Model;
import com.microsoft.z3.Solver;
import com.microsoft.z3.Status;

public class ProjectionsMain {

    private final int            a, b, c, ab, ac, bc, abc;
    private final List<Reltuple> fixed1List;
    private final Reltuple[]     rel;
    int                          varcount;
    int                          ntccount;

    public ProjectionsMain(int a, int b, int c, int ab, int ac, int bc, int abc, List<Reltuple> fixed1List) {
        this.a = a;
        this.b = b;
        this.c = c;
        this.ab = ab;
        this.ac = ac;
        this.bc = bc;
        this.abc = abc;

        this.fixed1List = fixed1List;

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

        for (Reltuple reltuple : fixed1List) {
            if (reltuple.aval >= a || reltuple.bval >= b || reltuple.cval >= c)
                return false;
        }

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

        //fixed1 constraints
        addFixed1cons(ctx, solver);

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
                    int value = Integer.parseInt(model.evaluate(ctx.mkIntConst(varname), true).toString());
                    if (value == 1) {
                        rel[++p] = new Reltuple(varname);
                    } else if (value != 0)
                        throw new RuntimeException("Found value=" + value + " for variable " + varname + " while expected 0 or 1");
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
        List<IntExpr> xabcAddList = new ArrayList<>(a * b * c);
        for (int i = 0; i < a; i++) {
            for (int j = 0; j < b; j++) {
                for (int k = 0; k < c; k++) {
                    String varname = "x_" + i + "_" + j + "_" + k;
                    varcount++;
                    solver.add(ctx.mkGe(ctx.mkIntConst(varname), ctx.mkInt(0)));
                    solver.add(ctx.mkLe(ctx.mkIntConst(varname), ctx.mkInt(1)));
                    xabcAddList.add(ctx.mkIntConst(varname));
                }
            }
        }
        solver.add(ctx.mkEq(ctx.mkAdd(xabcAddList.toArray(new ArithExpr[xabcAddList.size()])), ctx.mkInt(abc)));
        ntccount++;
    }

    private void addABcons(Context ctx, Solver solver) {
        List<IntExpr> yabAddList = new ArrayList<>(a * b);
        for (int i = 0; i < a; i++) {
            for (int j = 0; j < b; j++) {
                String varname = "yab_" + i + "_" + j;
                varcount++;
                solver.add(ctx.mkLe(ctx.mkIntConst(varname), ctx.mkInt(1)));
                yabAddList.add(ctx.mkIntConst(varname));

                List<IntExpr> addList = new ArrayList<>(c);
                for (int k = 0; k < c; k++) {
                    String temp = "x_" + i + "_" + j + "_" + k;
                    addList.add(ctx.mkIntConst(temp));
                    solver.add(ctx.mkGe(ctx.mkIntConst(varname), ctx.mkIntConst(temp)));
                    ntccount++;
                }
                solver.add(ctx.mkLe(ctx.mkIntConst(varname), ctx.mkAdd(addList.toArray(new ArithExpr[addList.size()]))));
                ntccount++;
            }
        }
        solver.add(ctx.mkEq(ctx.mkAdd(yabAddList.toArray(new ArithExpr[yabAddList.size()])), ctx.mkInt(ab)));
        ntccount++;
    }

    private void addACcons(Context ctx, Solver solver) {
        List<IntExpr> yacAddList = new ArrayList<>(a * c);
        for (int i = 0; i < a; i++) {
            for (int k = 0; k < c; k++) {
                String varname = "yac_" + i + "_" + k;
                varcount++;
                solver.add(ctx.mkLe(ctx.mkIntConst(varname), ctx.mkInt(1)));
                yacAddList.add(ctx.mkIntConst(varname));

                List<IntExpr> addList = new ArrayList<>(b);
                for (int j = 0; j < b; j++) {
                    String temp = "x_" + i + "_" + j + "_" + k;
                    addList.add(ctx.mkIntConst(temp));
                    solver.add(ctx.mkGe(ctx.mkIntConst(varname), ctx.mkIntConst(temp)));
                    ntccount++;
                }
                solver.add(ctx.mkLe(ctx.mkIntConst(varname), ctx.mkAdd(addList.toArray(new ArithExpr[addList.size()]))));
                ntccount++;
            }
        }
        solver.add(ctx.mkEq(ctx.mkAdd(yacAddList.toArray(new ArithExpr[yacAddList.size()])), ctx.mkInt(ac)));
        ntccount++;
    }

    private void addBCcons(Context ctx, Solver solver) {
        List<IntExpr> ybcAddList = new ArrayList<>(b * c);
        for (int j = 0; j < b; j++) {
            for (int k = 0; k < c; k++) {
                String varname = "ybc_" + j + "_" + k;
                varcount++;
                solver.add(ctx.mkLe(ctx.mkIntConst(varname), ctx.mkInt(1)));
                ybcAddList.add(ctx.mkIntConst(varname));

                List<IntExpr> addList = new ArrayList<>(a);
                for (int i = 0; i < a; i++) {
                    String temp = "x_" + i + "_" + j + "_" + k;
                    addList.add(ctx.mkIntConst(temp));
                    solver.add(ctx.mkGe(ctx.mkIntConst(varname), ctx.mkIntConst(temp)));
                    ntccount++;
                }
                solver.add(ctx.mkLe(ctx.mkIntConst(varname), ctx.mkAdd(addList.toArray(new ArithExpr[addList.size()]))));
                ntccount++;
            }
        }
        solver.add(ctx.mkEq(ctx.mkAdd(ybcAddList.toArray(new ArithExpr[ybcAddList.size()])), ctx.mkInt(bc)));
        ntccount++;
    }

    private void addAcons(Context ctx, Solver solver) {
        List<IntExpr> yaAddList = new ArrayList<>(a);
        for (int i = 0; i < a; i++) {
            String varname = "ya_" + i;
            varcount++;
            solver.add(ctx.mkLe(ctx.mkIntConst(varname), ctx.mkInt(1)));
            yaAddList.add(ctx.mkIntConst(varname));

            List<IntExpr> addList = new ArrayList<>(c);
            for (int j = 0; j < b; j++) {
                for (int k = 0; k < c; k++) {
                    String temp = "x_" + i + "_" + j + "_" + k;
                    addList.add(ctx.mkIntConst(temp));
                    solver.add(ctx.mkGe(ctx.mkIntConst(varname), ctx.mkIntConst(temp)));
                    ntccount++;
                }
            }
            solver.add(ctx.mkLe(ctx.mkIntConst(varname), ctx.mkAdd(addList.toArray(new ArithExpr[addList.size()]))));
            ntccount++;
        }
        solver.add(ctx.mkEq(ctx.mkAdd(yaAddList.toArray(new ArithExpr[yaAddList.size()])), ctx.mkInt(a)));
        ntccount++;
    }

    private void addBcons(Context ctx, Solver solver) {
        List<IntExpr> ybAddList = new ArrayList<>(b);
        for (int j = 0; j < b; j++) {
            String varname = "yb_" + j;
            varcount++;
            solver.add(ctx.mkLe(ctx.mkIntConst(varname), ctx.mkInt(1)));
            ybAddList.add(ctx.mkIntConst(varname));

            List<IntExpr> addList = new ArrayList<>(c);
            for (int i = 0; i < a; i++) {
                for (int k = 0; k < c; k++) {
                    String temp = "x_" + i + "_" + j + "_" + k;
                    addList.add(ctx.mkIntConst(temp));
                    solver.add(ctx.mkGe(ctx.mkIntConst(varname), ctx.mkIntConst(temp)));
                    ntccount++;
                }
            }
            solver.add(ctx.mkLe(ctx.mkIntConst(varname), ctx.mkAdd(addList.toArray(new ArithExpr[addList.size()]))));
            ntccount++;
        }
        solver.add(ctx.mkEq(ctx.mkAdd(ybAddList.toArray(new ArithExpr[ybAddList.size()])), ctx.mkInt(b)));
        ntccount++;
    }

    private void addCcons(Context ctx, Solver solver) {
        List<IntExpr> ycAddList = new ArrayList<>(c);
        for (int k = 0; k < c; k++) {
            String varname = "yc_" + k;
            varcount++;
            solver.add(ctx.mkLe(ctx.mkIntConst(varname), ctx.mkInt(1)));
            ycAddList.add(ctx.mkIntConst(varname));

            List<IntExpr> addList = new ArrayList<>(c);
            for (int i = 0; i < a; i++) {
                for (int j = 0; j < b; j++) {
                    String temp = "x_" + i + "_" + j + "_" + k;
                    addList.add(ctx.mkIntConst(temp));
                    solver.add(ctx.mkGe(ctx.mkIntConst(varname), ctx.mkIntConst(temp)));
                    ntccount++;
                }
            }
            solver.add(ctx.mkLe(ctx.mkIntConst(varname), ctx.mkAdd(addList.toArray(new ArithExpr[addList.size()]))));
            ntccount++;
        }
        solver.add(ctx.mkEq(ctx.mkAdd(ycAddList.toArray(new ArithExpr[ycAddList.size()])), ctx.mkInt(c)));
        ntccount++;
    }

    private void addFixed1cons(Context ctx, Solver solver) {
        for (Reltuple reltuple : fixed1List) {
            String temp = "x_" + reltuple.aval + "_" + reltuple.bval + "_" + reltuple.cval;
            solver.add(ctx.mkEq(ctx.mkIntConst(temp), ctx.mkInt(1)));
        }
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

        int a, b, c, ab, ac, bc, abc, fi, fj, fk;
        List<Reltuple> fixed1List;
        Scanner scanner = new Scanner(System.in);

        ProjectionsMain obj;
        do {
            System.out.print("Enter |A|, |B|, |C|, |AB|, |AC|, |BC|, |ABC|: ");
            a = scanner.nextInt();
            b = scanner.nextInt();
            c = scanner.nextInt();
            ab = scanner.nextInt();
            ac = scanner.nextInt();
            bc = scanner.nextInt();
            abc = scanner.nextInt();

            fixed1List = new ArrayList<>();
            System.out.print("Enter (i, j, k)s for which x are to be fixed to 1: ");
            while (true) {
                fi = scanner.nextInt();
                if (fi == -1) {
                    break;
                }
                fj = scanner.nextInt();
                fk = scanner.nextInt();

                Reltuple fixed1 = new Reltuple(fi, fj, fk);
                fixed1List.add(fixed1);
            }

            obj = new ProjectionsMain(a, b, c, ab, ac, bc, abc, fixed1List);
        } while (!obj.isInputValid());

        scanner.close();

        obj.solve();

        obj.displayRel(0, abc);
        obj.validateOutput(obj.abc);
    }
}

class Reltuple {

    public final int aval;
    public final int bval;
    public final int cval;

    public Reltuple(int av, int bv, int cv) {
        aval = av;
        bval = bv;
        cval = cv;
    }

    public Reltuple(String varname) { //x_a_b_c
        String[] arr = varname.split("_");
        aval = Integer.parseInt(arr[1]);
        bval = Integer.parseInt(arr[2]);
        cval = Integer.parseInt(arr[3]);
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + aval;
        result = prime * result + bval;
        result = prime * result + cval;
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        Reltuple other = (Reltuple) obj;
        if (aval != other.aval)
            return false;
        if (bval != other.bval)
            return false;
        if (cval != other.cval)
            return false;
        return true;
    }
}

class Intpair {
    public final int first;
    public final int second;

    public Intpair(int f, int s) {
        first = f;
        second = s;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + first;
        result = prime * result + second;
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        Intpair other = (Intpair) obj;
        if (first != other.first)
            return false;
        if (second != other.second)
            return false;
        return true;
    }
}

//3 5 7 10 15 25 40
//6 10 14 20 30 50 80
//30 50 70 1000 1500 2500 40000