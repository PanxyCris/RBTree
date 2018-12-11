import java.io.*;

public class RBTree {

    static final int RED = 0;
    static final int BLACK = 1;

    int key;
    RBTree left, right;
    int color; //0--red, 1--black

    private static RBTree rbt;

    public RBTree() {
    }

    public RBTree(RBTree l, int k, int c, RBTree r) {
        this.left = l;
        this.key = k;
        this.color = c;
        this.right = r;
    }

    /**
     * 增加操作
     *
     * @param v       增加元素
     * @param oldTree 老树
     * @return
     */
    private static InsReturn insert(int v, RBTree oldTree) {
        if (oldTree == null) {
            RBTree newTree = new RBTree(null, v, RED, null);
            Status status = Status.brb;
            return new InsReturn(newTree, status);
        } else {
            if (v < oldTree.key) {
                InsReturn ansLeft = insert(v, oldTree.left);
                return repairLeft(oldTree, ansLeft);
            } else {
                InsReturn ansRight = insert(v, oldTree.right);
                return repairRight(oldTree, ansRight);
            }
        }
    }

    /**
     * 修复左子树
     *
     * @param oldTree
     * @param ansLeft
     * @return
     */
    private static InsReturn repairLeft(RBTree oldTree, InsReturn ansLeft) {
        if (ansLeft.status == Status.ok)
            return new InsReturn(oldTree, Status.ok);
        oldTree.left = ansLeft.newTree;
        switch (ansLeft.status) {
            case rbr:
                return new InsReturn(oldTree, Status.ok);
            case brb:
                if (oldTree.color == BLACK)
                    return new InsReturn(oldTree, Status.ok);
                else
                    return new InsReturn(oldTree, Status.rrb);
            default:
                if (oldTree.right == null || oldTree.right.color == BLACK)
                    return new InsReturn(rebalLeft(oldTree, ansLeft.status), Status.rbr);
                else
                    return new InsReturn(colorFlip(oldTree), Status.brb);

        }
    }

    /**
     * 修复右子树
     *
     * @param oldTree
     * @param ansRight
     * @return
     */
    private static InsReturn repairRight(RBTree oldTree, InsReturn ansRight) {
        if (ansRight.status == Status.ok)
            return new InsReturn(oldTree, Status.ok);
        oldTree.right = ansRight.newTree;
        switch (ansRight.status) {
            case rbr:
                return new InsReturn(oldTree, Status.ok);
            case brb:
                if (oldTree.color == BLACK)
                    return new InsReturn(oldTree, Status.ok);
                else
                    return new InsReturn(oldTree, Status.brr);
            default:
                if (oldTree.left == null || oldTree.left.color == BLACK)
                    return new InsReturn(rebalRight(oldTree, ansRight.status), Status.rbr);
                else
                    return new InsReturn(colorFlip(oldTree), Status.brb);

        }
    }

    /**
     * 增加操作的左部调整
     *
     * @param oldTree
     * @param leftStatus
     * @return
     */
    private static RBTree rebalLeft(RBTree oldTree, Status leftStatus) {
        RBTree L, M, R, LR, RL;
        if (leftStatus == Status.rrb) {
            R = oldTree;
            M = oldTree.left;
            L = M.left;
            RL = M.right;
            R.left = RL;
            M.right = R;
        } else {
            R = oldTree;
            L = oldTree.left;
            M = L.right;
            LR = M.left;
            RL = M.right;
            R.left = RL;
            L.right = LR;
            M.right = R;
            M.left = L;
        }
        L.color = RED;
        R.color = RED;
        M.color = BLACK;
        return M;
    }

    /**
     * 增加操作的右部调整
     *
     * @param oldTree
     * @param rightStatus
     * @return
     */
    private static RBTree rebalRight(RBTree oldTree, Status rightStatus) {
        RBTree L, M, R;
        if (rightStatus == Status.brr) {
            L = oldTree;
            M = oldTree.right;
            R = M.right;
            L.right = M.left;
            M.left = L;
        } else {
            L = oldTree;
            R = oldTree.right;
            M = R.left;
            L.right = M.left;
            R.left = M.right;
            M.right = R;
            M.left = L;
        }
        L.color = RED;
        R.color = RED;
        M.color = BLACK;
        return M;
    }

    /**
     * 颜色翻转
     *
     * @param oldTree
     * @return
     */
    private static RBTree colorFlip(RBTree oldTree) {
        if (oldTree.color == BLACK)
            oldTree.color = RED;
        else
            oldTree.color = BLACK;
        if (oldTree.left.color == BLACK)
            oldTree.left.color = RED;
        else
            oldTree.left.color = BLACK;
        if (oldTree.right.color == BLACK)
            oldTree.right.color = RED;
        else
            oldTree.right.color = BLACK;
        return oldTree;
    }

    /**
     * 删除操作
     *
     * @param v    删除元素
     * @param tree 树
     * @return
     */
    private static DelReturn delete(int v, RBTree tree) {
        if (tree == null)
            throw new RuntimeException("The element " + v + " is not exist");
        DelReturn delReturn;
        if (v < tree.key) {
            delReturn = delete(v, tree.left);
            tree.left = delReturn.newTree;
            if (delReturn.status == DelStatus.undefined)
                delReturn.status = DelStatus.leftAdjust;
        } else if (v > tree.key) {
            delReturn = delete(v, tree.right);
            tree.right = delReturn.newTree;
            if (delReturn.status == DelStatus.undefined)
                delReturn.status = DelStatus.rightAdjust;
        } else if (tree.left != null && tree.right != null) { //非叶子节点
            tree.key = findMin(tree.right);
            delReturn = delete(tree.key, tree.right);
            tree.right = delReturn.newTree;
            if (delReturn.status == DelStatus.undefined)
                delReturn.status = DelStatus.rightAdjust;
        } else if (tree.color == RED) { //表示两个子树都为空了
            tree = tree.right;
            delReturn = new DelReturn(tree, DelStatus.ok);
        } else if (tree.right != null) { //也意味着左子树为空，右子树为红色
            tree = tree.right;
            tree.color = BLACK;
            delReturn = new DelReturn(tree, DelStatus.ok);
        } else if (tree.left != null) {//右子树为空，左子树为红色
            tree = tree.left;
            tree.color = BLACK;
            delReturn = new DelReturn(tree, DelStatus.ok);
        } else {//意味着删除的节点是黑色的，且左右子树均为空
            return new DelReturn(null, DelStatus.undefined);
        }
        if (delReturn.status == DelStatus.ok) {
            delReturn.newTree = tree;
            return delReturn;
        } else {
            if (delReturn.status == DelStatus.leftAdjust)
                return undefinedRepairLeft(tree);
            else
                return undefinedRepairRight(tree);
        }
    }

    /**
     * 删除修复左子
     *
     * @param tree
     * @return
     */
    private static DelReturn undefinedRepairLeft(RBTree tree) {
        if (tree.right.color == RED) {
            RBTree M = tree.right;
            RBTree L = tree;
            RBTree LR = M.left;
            L.right = LR;
            M.left = L;
            L.color = RED;
            M.color = BLACK;
            DelReturn delReturn = undefinedRepairLeft(L);
            M.left = delReturn.newTree;
            return new DelReturn(M, delReturn.status);
        } else {
            if (tree.right.left != null && tree.right.left.color == RED) {
                int MColor = tree.color;
                RBTree R = tree.right;
                RBTree M = R.left;
                RBTree L = tree;
                R.left = M.right;
                L.right = M.left;
                M.right = R;
                M.left = L;
                L.color = BLACK;
                M.color = MColor;
                return new DelReturn(M, DelStatus.ok);
            } else if (tree.color == RED && (tree.right.left == null || tree.right.left.color == BLACK)) {
                RBTree M = tree.right;
                RBTree LR = M.left;
                RBTree L = tree;
                L.right = LR;
                M.left = L;
                if (L.left != null)
                    L.left.color = BLACK;
                return new DelReturn(M, DelStatus.ok);
            } else if (tree.right.right != null && tree.right.right.color == RED) {
                int MColor = tree.color;
                RBTree M = tree.right;
                RBTree L = tree;
                RBTree R = M.right;
                RBTree LR = M.left;
                L.right = LR;
                M.left = L;
                L.color = BLACK;
                R.color = BLACK;
                M.color = MColor;
                return new DelReturn(M, DelStatus.ok);
            } else if (tree.color == RED) {
                RBTree M = tree.right;
                RBTree L = tree;
                L.right = M.left;
                M.left = L;
                return new DelReturn(M, DelStatus.ok);
            } else {
                tree.right.color = RED;
                return new DelReturn(tree, DelStatus.undefined);
            }
        }
    }

    /**
     * 删除修复右子
     *
     * @param tree
     * @return
     */
    private static DelReturn undefinedRepairRight(RBTree tree) {
        if (tree.left.color == RED) {
            RBTree R = tree;
            RBTree M = tree.left;
            RBTree RL = M.right;
            R.left = RL;
            M.right = R;
            R.color = RED;
            M.color = BLACK;
            DelReturn delReturn = undefinedRepairRight(M.right);
            M.right = delReturn.newTree;
            return new DelReturn(M, delReturn.status);
        } else {
            if (tree.left.right != null && tree.left.right.color == RED) {
                int MColor = tree.color;
                RBTree L = tree.left;
                RBTree M = L.right;
                RBTree R = tree;
                L.right = M.left;
                R.left = M.right;
                M.right = R;
                M.left = L;
                R.color = BLACK;
                M.color = MColor;
                return new DelReturn(M, DelStatus.ok);
            } else if (tree.color == RED && (tree.left.right == null || tree.left.right.color == BLACK)) {
                RBTree M = tree.left;
                RBTree RL = M.right;
                RBTree R = tree;
                R.left = RL;
                M.right = R;
                if (R.right != null)
                    R.right.color = BLACK;
                return new DelReturn(M, DelStatus.ok);
            } else if (tree.left.left != null && tree.left.left.color == RED) {
                int MColor = tree.color;
                RBTree M = tree.left;
                RBTree R = tree;
                RBTree L = M.left;
                RBTree RL = M.right;
                R.left = RL;
                M.right = R;
                L.color = BLACK;
                R.color = BLACK;
                M.color = MColor;
                return new DelReturn(M, DelStatus.ok);
            } else if (tree.color == RED) {
                RBTree M = tree.left;
                RBTree R = tree;
                R.left = M.right;
                M.right = R;
                return new DelReturn(M, DelStatus.ok);
            } else {
                tree.left.color = RED;
                return new DelReturn(tree, DelStatus.undefined);
            }
        }
    }

    /**
     * 获取最小值
     *
     * @param tree
     * @return
     */
    private static int findMin(RBTree tree) {
        while (tree.left != null)
            tree = tree.left;
        return tree.key;
    }

    /**
     * 获取黑色高度
     *
     * @return
     */
    int getBlackHeight() {
        int lHeight;
        if (this.left == null)
            lHeight = 1;
        else {
            lHeight = this.left.getBlackHeight();
            if (lHeight < 0)
                return -1;
        }

        int rHeight;
        if (this.right == null)
            rHeight = 1;
        else {
            rHeight = this.right.getBlackHeight();
            if (rHeight < 0)
                return -1;
        }

        if (lHeight == rHeight)
            return lHeight + (this.color == BLACK ? 1 : 0);

        return -1;
    }

    private static boolean checkOrder(RBTree rbt) {
        curValue = -1;
        return checkOrder_Rec(rbt);
    }

    private static int curValue;

    private static boolean checkOrder_Rec(RBTree rbt) {
        if (rbt == null)
            return true;

        if (!checkOrder_Rec(rbt.left))
            return false;

        if (rbt.key < curValue)
            return false;
        curValue = rbt.key;

        if (!checkOrder_Rec(rbt.right))
            return false;

        return true;
    }

    /**
     * 输出树中元素
     *
     * @param rbt
     * @return
     */
    private static boolean outputElements_Rec(RBTree rbt) {
        if (rbt == null)
            return true;

        if (!outputElements_Rec(rbt.left))
            return false;

        System.out.print("\t" + rbt.key);

        if (!outputElements_Rec(rbt.right))
            return false;

        return true;
    }

    /**
     * 调整颜色
     *
     * @return
     */
    boolean checkColor() {
        int leftColor;
        if (this.left == null)
            leftColor = BLACK;
        else {
            if (!this.left.checkColor())
                return false;
            leftColor = this.left.color;
        }
        int rightColor;
        if (this.right == null)
            rightColor = BLACK;
        else {
            if (!this.right.checkColor())
                return false;
            rightColor = this.right.color;
        }

        if (this.color == BLACK)
            return true;
        if (leftColor == RED || rightColor == RED)
            return false;
        return true;
    }


    public String toString() {
        String s = "(";
        if (this.left == null)
            s += "NIL";
        else
            s += this.left.toString();
        s += ", (";
        s += "" + this.key + "," + (this.color == RED ? "red" : "black") + "), ";

        if (this.right == null)
            s += "NIL";
        else
            s += this.right.toString();

        s += ")";

        return s;
    }


    static String errorMsg;
    static int nextChar;
    static int nextPos;
    static char[] inputBuf;
    static String scannedInput;


    /**
     * 获得输入
     *
     * @param s
     * @return
     */
    static void constrctOriginRBTree(String s) {
        inputBuf = s.toCharArray();
        errorMsg = null;
        nextChar = inputBuf[0];
        nextPos = 0;
        rbt = getRBTFromInput_Rec();
//        if (errorMsg != null) {
//            System.out.println("\t" + s.substring(0, nextPos));
//            System.out.println("\t" + errorMsg);
//        }
    }

    /**
     * 获取当前红黑树
     *
     * @return
     */
    private static RBTree getRBTFromInput_Rec() {
        if (tryToGetNIL())
            return null;

        getLeftPara();
        if (errorMsg != null)
            return null;

        RBTree left = getRBTFromInput_Rec();
        if (errorMsg != null)
            return null;

        getCOMMA();
        if (errorMsg != null)
            return null;

        getLeftPara();
        if (errorMsg != null)
            return null;

        int key = getInteger();
        if (errorMsg != null)
            return null;

        getCOMMA();
        if (errorMsg != null)
            return null;

        int color = getCOLOR();
        if (errorMsg != null)
            return null;

        getRightPara();
        if (errorMsg != null)
            return null;

        getCOMMA();
        if (errorMsg != null)
            return null;

        RBTree right = getRBTFromInput_Rec();
        if (errorMsg != null)
            return null;

        getRightPara();
        if (errorMsg != null)
            return null;

        return new RBTree(left, key, color, right);
    }

    private static void getCOMMA() {
        skipBlank();
        if (nextChar == ',') {
            nextChar = getAChar();
            skipBlank();
        } else {
            errorMsg = "\',\' expected!";
        }
    }

    private static void getLeftPara() {
        skipBlank();
        if (nextChar == '(') {
            nextChar = getAChar();
            skipBlank();
        } else {
            errorMsg = "\'(\' expected!";
        }
    }

    private static void getRightPara() {
        skipBlank();
        if (nextChar == ')') {
            nextChar = getAChar();
            skipBlank();
        } else {
            errorMsg = "\')\' expected!";
        }
    }

    /**
     * 空值节点
     *
     * @return
     */
    static private boolean tryToGetNIL() {
        int prevPos = nextPos;
        skipBlank();
        String id = getID();
        if ("NIL".equals(id))
            return true;

        nextPos = prevPos;
        nextChar = nextPos < inputBuf.length ? inputBuf[nextPos] : -1;

//		errorMsg = "\"NIL\" expected.";
        return false;
    }

    /**
     * 获取颜色
     *
     * @return
     */
    private static int getCOLOR() {
        skipBlank();
        String id = getID();
        if ("black".equals(id))
            return BLACK;
        else if ("red".equals(id))
            return RED;
        else
            errorMsg = "Black/Red expected.";
        return -1;
    }

    /**
     * 获得字母，如nil，red和black
     *
     * @return
     */
    private static String getID() {
        String ret = "";
        while ((nextChar >= 'a' && nextChar <= 'z') || (nextChar >= 'A' && nextChar <= 'Z')) {
            ret += (char) nextChar;
            nextChar = getAChar();
        }
        return ret;
    }

    /**
     * 获得整数
     *
     * @return
     */
    private static int getInteger() {
        int ret = 0;
        while (nextChar >= '0' && nextChar <= '9') {
            ret = ret * 10 + (nextChar - '0');
            nextChar = getAChar();
        }
        if ((nextChar >= 'a' && nextChar <= 'z') || (nextChar >= 'A' && nextChar == 'Z')) {
            errorMsg = "A char following an integer.";
        }
        return ret;
    }

    /**
     * 跳过空白
     */
    private static void skipBlank() {
        while (nextChar == ' ' || nextChar == '\n' || nextChar == '\t')
            nextChar = getAChar();
    }

    /**
     * 获得当前缓冲区字符
     *
     * @return
     */
    private static int getAChar() {
        nextPos++;
        if (nextPos >= inputBuf.length)
            nextChar = -1;
        else
            nextChar = inputBuf[nextPos];
        return nextChar;
    }

    /**
     * 测试
     */
    public void test() {
        String dir = "resource/final_redblack_data/";
        String outDir = "resource/out/";
//        int n = 9;
//        int all = 15;
//        String command = "t";
        int n = 10;
//        int all = 15;
        String command = "rb";
        int notPassCase = 0;
        for (int i = 0; i < n; i++) {
            String origin = "(NIL)";
            constrctOriginRBTree(origin);
            String outputContent = "";
            boolean isError = false;
            String path = dir + command + i + ".";
            String inFile = path + "in";
            String outFile = path + "out";
            File iFile = new File(inFile);
            File oFile = new File(outFile);
            try {
                BufferedReader rw = new BufferedReader(new InputStreamReader(new FileInputStream(iFile)));
                BufferedReader of = new BufferedReader(new InputStreamReader(new FileInputStream(oFile)));
                String line = rw.readLine();
                if (line == null)
                    continue;
                int m = Integer.parseInt(line);
                for (int j = 1; j <= m; j++) {
                    String orders = rw.readLine();
                    String[] order = orders.split(" ");
                    int v = Integer.parseInt(order[1]);
                    switch (order[0]) {
                        case "a":
                            rbt = insert(v, rbt).newTree;
                            if (rbt.color == RED)
                                rbt.color = BLACK;
                            break;
                        case "d":
                            rbt = delete(v, rbt).newTree;
                            break;
                        default:
                            throw new RuntimeException("It's illegal");
                    }
                    String out = of.readLine();
                    String result = rbt.toString();
                    if (!result.equals(out)) {
                        if (!isError) {
                            System.out.println(i + ":" + j + " Exists error");
                            System.out.println("my:" + result);
                            System.out.println("st:" + out);
                            System.out.println();
                            isError = true;
                        }
                    } else
                        outputContent += result + "\n";
//
                }
                rw.close();
                of.close();


            } catch (Exception ex) {
                ex.printStackTrace();
            }
            if (isError)
                notPassCase++;
            else {
                String outputFile = outDir + "rb" + i + ".out";
                try {
                    File file = new File(outputFile);
                    FileWriter writer = new FileWriter(file, false);
                    PrintWriter printWriter = new PrintWriter(writer);
                    printWriter.write(outputContent);
                    printWriter.println();
                    writer.close();
                    printWriter.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
//            if (i == n) {
//                if (command.equals("t")) {
//                    command = "o";
//                    i = 0;
//                    n = 3;
//                } else if (command.equals("o")) {
//                    command = "b";
//                    i = 0;
//                }
//            }

        }
//        System.out.println("Result:" + (all - notPassCase) + "/" + all);
        System.out.println("Result:" + (n - notPassCase) + "/" + n);
        if (notPassCase == 0)
            System.out.println("AC,great!");
        else
            System.out.println("Error,Please improve.");
    }

    public static void main(String[] args) {
        new RBTree().test();
//        Scanner input = new Scanner(System.in);
////        String origin = input.nextLine();
////        String origin = "(NIL)";
//        String origin = "((NIL, (7,black), NIL), (8,black), (NIL, (9,black), (NIL, (10,red), NIL)))";
//        constrctOriginRBTree(origin);
//
//        int m = Integer.parseInt(input.nextLine());
//        for (int i = 0; i < m; i++) {
//            String orders = input.nextLine();
//            String[] order = orders.split(" ");
//            int v = Integer.parseInt(order[1]);
//            switch (order[0]) {
//                case "a":
//                    rbt = insert(v, rbt).newTree;
//                    if (rbt.color == RED)
//                        rbt.color = BLACK;
//                    break;
//                case "d":
//                    rbt = delete(v, rbt).newTree;
//                    break;
//                default:
//                    throw new RuntimeException("It's illegal");
//            }
//        }
//        if (rbt != null) {
//            System.out.println("Output\t" + rbt.toString());
//            int h = rbt.getBlackHeight();
//            boolean colorCheck = rbt.checkColor();
//            System.out.println("\tColor constraint " + (colorCheck ? "OK" : "broken"));
//            System.out.println("\tBlack height " + (h >= 0 ? " is " + h : "constraint broken"));
//            System.out.print("\tElements in tree:");
//            RBTree.outputElements_Rec(rbt);
//            boolean orderCheck = RBTree.checkOrder(rbt);
//            System.out.println("\n\tOrder constraint " + (orderCheck ? "OK" : "broken"));
//        }
//
//        System.out.println("nextChar:" + nextChar);
//        System.out.println("nextPos:" + nextPos);
//        System.out.print("inputBuf:");
//        for (int i = 0; i < inputBuf.length; i++)
//            System.out.print(inputBuf[i]);
//
//
    }

}
