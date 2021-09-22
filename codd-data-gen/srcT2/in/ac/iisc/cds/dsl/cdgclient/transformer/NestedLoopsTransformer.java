package in.ac.iisc.cds.dsl.cdgclient.transformer;

import in.ac.iisc.cds.dsl.cdgclient.constants.PostgresCConfig;
import in.ac.iisc.cds.dsl.cdgvendor.model.AlqpInternalNode;
import in.ac.iisc.cds.dsl.cdgvendor.model.AlqpLeafNode;
import in.ac.iisc.cds.dsl.cdgvendor.model.AlqpNode;

/**
 * Applies Rule1: Nested Loops join is changed into Hash Join
 * @author raghav
 *
 */
public class NestedLoopsTransformer {

    private static boolean ruleApplies(AlqpNode root) {
        if (root == null || !"Nested Loop".equals(root.getNodetype()))
            return false;

        AlqpInternalNode node = (AlqpInternalNode) root;

        if (node.getRightChild() == null)
            return false;

        return true;
    }

    public static void transformIfApplicable(AlqpNode root) {

        if (ruleApplies(root)) {
            AlqpInternalNode node = (AlqpInternalNode) root;
            AlqpNode rightChild = node.getRightChild();

            boolean isRightChildMorphed = false;
            if ("Bitmap Heap Scan".equals(rightChild.getNodetype())) {
                //See query3.json
                AlqpInternalNode bhs = (AlqpInternalNode) rightChild;

                AlqpLeafNode leaf = new AlqpLeafNode();
                leaf.setRelname(bhs.getRelname());
                leaf.setAlias(bhs.getAlias());
                //leaf.setConditionStr(bhs.getLeftChild().getConditionStr());   TODO: As there are no predicates on keys, there won't be any useful filter here
                leaf.setOutputCardinality(PostgresCConfig.BASICSCHEMA_INFO.getTableInfo(bhs.getRelname()).getRowcount());
                leaf.setNodetype(bhs.getNodetype() + " MORPHED");
                node.setRightChild(leaf);

                isRightChildMorphed = true;

            } else if ("Index Scan".equals(rightChild.getNodetype())) {
                //See query50.json
                AlqpLeafNode is = (AlqpLeafNode) rightChild;

                AlqpLeafNode leaf = new AlqpLeafNode();
                leaf.setRelname(is.getRelname());
                leaf.setAlias(is.getAlias());
                //leaf.setConditionStr(is.getIndexConditionStr());   TODO: As there are no predicates on keys, there won't be any useful filter here
                leaf.setOutputCardinality(PostgresCConfig.BASICSCHEMA_INFO.getTableInfo(is.getRelname()).getRowcount());
                leaf.setNodetype(is.getNodetype() + " MORPHED");
                node.setRightChild(leaf);

                isRightChildMorphed = true;

            } else if ("Seq Scan".equals(rightChild.getNodetype())) {

                isRightChildMorphed = true;
            }

            node.setConditionStr(rightChild.getConditionStr());
            rightChild.setConditionStr(null);
            node.setNodetype(node.getNodetype() + " MORPHED");

            transformIfApplicable(((AlqpInternalNode) root).getLeftChild());

            if (!isRightChildMorphed) {
                transformIfApplicable(((AlqpInternalNode) root).getRightChild());
            }

        } else if (root instanceof AlqpInternalNode) {
            transformIfApplicable(((AlqpInternalNode) root).getLeftChild());
            transformIfApplicable(((AlqpInternalNode) root).getRightChild());
        }

    }

}
