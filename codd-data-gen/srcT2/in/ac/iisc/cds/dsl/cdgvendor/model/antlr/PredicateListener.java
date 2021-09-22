package in.ac.iisc.cds.dsl.cdgvendor.model.antlr;// Generated from Predicate.g by ANTLR 4.5.3
import org.antlr.v4.runtime.tree.ParseTreeListener;

/**
 * This interface defines a complete listener for a parse tree produced by
 * {@link PredicateParser}.
 */
public interface PredicateListener extends ParseTreeListener {
	/**
	 * Enter a parse tree produced by {@link PredicateParser#predicate}.
	 * @param ctx the parse tree
	 */
	void enterPredicate(PredicateParser.PredicateContext ctx);
	/**
	 * Exit a parse tree produced by {@link PredicateParser#predicate}.
	 * @param ctx the parse tree
	 */
	void exitPredicate(PredicateParser.PredicateContext ctx);
	/**
	 * Enter a parse tree produced by {@link PredicateParser#conditionStr}.
	 * @param ctx the parse tree
	 */
	void enterConditionStr(PredicateParser.ConditionStrContext ctx);
	/**
	 * Exit a parse tree produced by {@link PredicateParser#conditionStr}.
	 * @param ctx the parse tree
	 */
	void exitConditionStr(PredicateParser.ConditionStrContext ctx);
	/**
	 * Enter a parse tree produced by {@link PredicateParser#condition}.
	 * @param ctx the parse tree
	 */
	void enterCondition(PredicateParser.ConditionContext ctx);
	/**
	 * Exit a parse tree produced by {@link PredicateParser#condition}.
	 * @param ctx the parse tree
	 */
	void exitCondition(PredicateParser.ConditionContext ctx);
	/**
	 * Enter a parse tree produced by {@link PredicateParser#andedCondition}.
	 * @param ctx the parse tree
	 */
	void enterAndedCondition(PredicateParser.AndedConditionContext ctx);
	/**
	 * Exit a parse tree produced by {@link PredicateParser#andedCondition}.
	 * @param ctx the parse tree
	 */
	void exitAndedCondition(PredicateParser.AndedConditionContext ctx);
	/**
	 * Enter a parse tree produced by {@link PredicateParser#oredCondition}.
	 * @param ctx the parse tree
	 */
	void enterOredCondition(PredicateParser.OredConditionContext ctx);
	/**
	 * Exit a parse tree produced by {@link PredicateParser#oredCondition}.
	 * @param ctx the parse tree
	 */
	void exitOredCondition(PredicateParser.OredConditionContext ctx);
	/**
	 * Enter a parse tree produced by {@link PredicateParser#joinCondition}.
	 * @param ctx the parse tree
	 */
	void enterJoinCondition(PredicateParser.JoinConditionContext ctx);
	/**
	 * Exit a parse tree produced by {@link PredicateParser#joinCondition}.
	 * @param ctx the parse tree
	 */
	void exitJoinCondition(PredicateParser.JoinConditionContext ctx);
	/**
	 * Enter a parse tree produced by {@link PredicateParser#simpleCondition}.
	 * @param ctx the parse tree
	 */
	void enterSimpleCondition(PredicateParser.SimpleConditionContext ctx);
	/**
	 * Exit a parse tree produced by {@link PredicateParser#simpleCondition}.
	 * @param ctx the parse tree
	 */
	void exitSimpleCondition(PredicateParser.SimpleConditionContext ctx);
	/**
	 * Enter a parse tree produced by {@link PredicateParser#simpleNumCondition}.
	 * @param ctx the parse tree
	 */
	void enterSimpleNumCondition(PredicateParser.SimpleNumConditionContext ctx);
	/**
	 * Exit a parse tree produced by {@link PredicateParser#simpleNumCondition}.
	 * @param ctx the parse tree
	 */
	void exitSimpleNumCondition(PredicateParser.SimpleNumConditionContext ctx);
	/**
	 * Enter a parse tree produced by {@link PredicateParser#simpleStrCondition}.
	 * @param ctx the parse tree
	 */
	void enterSimpleStrCondition(PredicateParser.SimpleStrConditionContext ctx);
	/**
	 * Exit a parse tree produced by {@link PredicateParser#simpleStrCondition}.
	 * @param ctx the parse tree
	 */
	void exitSimpleStrCondition(PredicateParser.SimpleStrConditionContext ctx);
	/**
	 * Enter a parse tree produced by {@link PredicateParser#simpleDateCondition}.
	 * @param ctx the parse tree
	 */
	void enterSimpleDateCondition(PredicateParser.SimpleDateConditionContext ctx);
	/**
	 * Exit a parse tree produced by {@link PredicateParser#simpleDateCondition}.
	 * @param ctx the parse tree
	 */
	void exitSimpleDateCondition(PredicateParser.SimpleDateConditionContext ctx);
	/**
	 * Enter a parse tree produced by {@link PredicateParser#simpleINCondition}.
	 * @param ctx the parse tree
	 */
	void enterSimpleINCondition(PredicateParser.SimpleINConditionContext ctx);
	/**
	 * Exit a parse tree produced by {@link PredicateParser#simpleINCondition}.
	 * @param ctx the parse tree
	 */
	void exitSimpleINCondition(PredicateParser.SimpleINConditionContext ctx);
	/**
	 * Enter a parse tree produced by {@link PredicateParser#and}.
	 * @param ctx the parse tree
	 */
	void enterAnd(PredicateParser.AndContext ctx);
	/**
	 * Exit a parse tree produced by {@link PredicateParser#and}.
	 * @param ctx the parse tree
	 */
	void exitAnd(PredicateParser.AndContext ctx);
	/**
	 * Enter a parse tree produced by {@link PredicateParser#or}.
	 * @param ctx the parse tree
	 */
	void enterOr(PredicateParser.OrContext ctx);
	/**
	 * Exit a parse tree produced by {@link PredicateParser#or}.
	 * @param ctx the parse tree
	 */
	void exitOr(PredicateParser.OrContext ctx);
	/**
	 * Enter a parse tree produced by {@link PredicateParser#any}.
	 * @param ctx the parse tree
	 */
	void enterAny(PredicateParser.AnyContext ctx);
	/**
	 * Exit a parse tree produced by {@link PredicateParser#any}.
	 * @param ctx the parse tree
	 */
	void exitAny(PredicateParser.AnyContext ctx);
	/**
	 * Enter a parse tree produced by {@link PredicateParser#columnname}.
	 * @param ctx the parse tree
	 */
	void enterColumnname(PredicateParser.ColumnnameContext ctx);
	/**
	 * Exit a parse tree produced by {@link PredicateParser#columnname}.
	 * @param ctx the parse tree
	 */
	void exitColumnname(PredicateParser.ColumnnameContext ctx);
	/**
	 * Enter a parse tree produced by {@link PredicateParser#symbol}.
	 * @param ctx the parse tree
	 */
	void enterSymbol(PredicateParser.SymbolContext ctx);
	/**
	 * Exit a parse tree produced by {@link PredicateParser#symbol}.
	 * @param ctx the parse tree
	 */
	void exitSymbol(PredicateParser.SymbolContext ctx);
	/**
	 * Enter a parse tree produced by {@link PredicateParser#number}.
	 * @param ctx the parse tree
	 */
	void enterNumber(PredicateParser.NumberContext ctx);
	/**
	 * Exit a parse tree produced by {@link PredicateParser#number}.
	 * @param ctx the parse tree
	 */
	void exitNumber(PredicateParser.NumberContext ctx);
	/**
	 * Enter a parse tree produced by {@link PredicateParser#stringliteral}.
	 * @param ctx the parse tree
	 */
	void enterStringliteral(PredicateParser.StringliteralContext ctx);
	/**
	 * Exit a parse tree produced by {@link PredicateParser#stringliteral}.
	 * @param ctx the parse tree
	 */
	void exitStringliteral(PredicateParser.StringliteralContext ctx);
}