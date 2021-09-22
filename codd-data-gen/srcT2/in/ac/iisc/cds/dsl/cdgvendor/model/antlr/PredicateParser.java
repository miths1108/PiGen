package in.ac.iisc.cds.dsl.cdgvendor.model.antlr;// Generated from Predicate.g by ANTLR 4.5.3
import org.antlr.v4.runtime.atn.*;
import org.antlr.v4.runtime.dfa.DFA;
import org.antlr.v4.runtime.*;
import org.antlr.v4.runtime.misc.*;
import org.antlr.v4.runtime.tree.*;
import java.util.List;
import java.util.Iterator;
import java.util.ArrayList;

@SuppressWarnings({"all", "warnings", "unchecked", "unused", "cast"})
public class PredicateParser extends Parser {
	static { RuntimeMetaData.checkVersion("4.5.3", RuntimeMetaData.VERSION); }

	protected static final DFA[] _decisionToDFA;
	protected static final PredictionContextCache _sharedContextCache =
		new PredictionContextCache();
	public static final int
		T__0=1, T__1=2, T__2=3, T__3=4, T__4=5, T__5=6, T__6=7, T__7=8, T__8=9, 
		T__9=10, T__10=11, T__11=12, T__12=13, T__13=14, T__14=15, T__15=16, T__16=17, 
		T__17=18, T__18=19, T__19=20, T__20=21, T__21=22, T__22=23, T__23=24, 
		T__24=25, OPEN=26, CLOSE=27, QUOTE=28, DOT=29, TEXT=30, SPACE=31, INTEGER=32, 
		DECIMAL=33, DATE=34, TIMESTAMP=35;
	public static final int
		RULE_predicate = 0, RULE_conditionStr = 1, RULE_condition = 2, RULE_andedCondition = 3, 
		RULE_oredCondition = 4, RULE_joinCondition = 5, RULE_simpleCondition = 6, 
		RULE_simpleNumCondition = 7, RULE_simpleStrCondition = 8, RULE_simpleDateCondition = 9, 
		RULE_simpleINCondition = 10, RULE_and = 11, RULE_or = 12, RULE_any = 13, 
		RULE_columnname = 14, RULE_symbol = 15, RULE_number = 16, RULE_stringliteral = 17;
	public static final String[] ruleNames = {
		"predicate", "conditionStr", "condition", "andedCondition", "oredCondition", 
		"joinCondition", "simpleCondition", "simpleNumCondition", "simpleStrCondition", 
		"simpleDateCondition", "simpleINCondition", "and", "or", "any", "columnname", 
		"symbol", "number", "stringliteral"
	};

	private static final String[] _LITERAL_NAMES = {
		null, "'='", "'::numeric'", "'::text'", "'::bpchar'", "'::integer'", "'::date'", 
		"'::timestamp without time zone'", "'{'", "','", "'}'", "'::text[]'", 
		"'::bpchar[]'", "'::numeric[]'", "'::integer[]'", "'AND'", "'OR'", "'ANY'", 
		"'<'", "'>'", "'>='", "'<='", "'#'", "'\\'", "'\"'", "':'", "'('", "')'", 
		"'''", "'.'", null, "' '"
	};
	private static final String[] _SYMBOLIC_NAMES = {
		null, null, null, null, null, null, null, null, null, null, null, null, 
		null, null, null, null, null, null, null, null, null, null, null, null, 
		null, null, "OPEN", "CLOSE", "QUOTE", "DOT", "TEXT", "SPACE", "INTEGER", 
		"DECIMAL", "DATE", "TIMESTAMP"
	};
	public static final Vocabulary VOCABULARY = new VocabularyImpl(_LITERAL_NAMES, _SYMBOLIC_NAMES);

	/**
	 * @deprecated Use {@link #VOCABULARY} instead.
	 */
	@Deprecated
	public static final String[] tokenNames;
	static {
		tokenNames = new String[_SYMBOLIC_NAMES.length];
		for (int i = 0; i < tokenNames.length; i++) {
			tokenNames[i] = VOCABULARY.getLiteralName(i);
			if (tokenNames[i] == null) {
				tokenNames[i] = VOCABULARY.getSymbolicName(i);
			}

			if (tokenNames[i] == null) {
				tokenNames[i] = "<INVALID>";
			}
		}
	}

	@Override
	@Deprecated
	public String[] getTokenNames() {
		return tokenNames;
	}

	@Override

	public Vocabulary getVocabulary() {
		return VOCABULARY;
	}

	@Override
	public String getGrammarFileName() { return "Predicate.g"; }

	@Override
	public String[] getRuleNames() { return ruleNames; }

	@Override
	public String getSerializedATN() { return _serializedATN; }

	@Override
	public ATN getATN() { return _ATN; }

	public PredicateParser(TokenStream input) {
		super(input);
		_interp = new ParserATNSimulator(this,_ATN,_decisionToDFA,_sharedContextCache);
	}
	public static class PredicateContext extends ParserRuleContext {
		public ConditionStrContext conditionStr() {
			return getRuleContext(ConditionStrContext.class,0);
		}
		public PredicateContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_predicate; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof PredicateListener ) ((PredicateListener)listener).enterPredicate(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof PredicateListener ) ((PredicateListener)listener).exitPredicate(this);
		}
	}

	public final PredicateContext predicate() throws RecognitionException {
		PredicateContext _localctx = new PredicateContext(_ctx, getState());
		enterRule(_localctx, 0, RULE_predicate);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(36);
			conditionStr();
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class ConditionStrContext extends ParserRuleContext {
		public TerminalNode OPEN() { return getToken(PredicateParser.OPEN, 0); }
		public ConditionContext condition() {
			return getRuleContext(ConditionContext.class,0);
		}
		public TerminalNode CLOSE() { return getToken(PredicateParser.CLOSE, 0); }
		public ConditionStrContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_conditionStr; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof PredicateListener ) ((PredicateListener)listener).enterConditionStr(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof PredicateListener ) ((PredicateListener)listener).exitConditionStr(this);
		}
	}

	public final ConditionStrContext conditionStr() throws RecognitionException {
		ConditionStrContext _localctx = new ConditionStrContext(_ctx, getState());
		enterRule(_localctx, 2, RULE_conditionStr);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(38);
			match(OPEN);
			setState(39);
			condition();
			setState(40);
			match(CLOSE);
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class ConditionContext extends ParserRuleContext {
		public AndedConditionContext andedCondition() {
			return getRuleContext(AndedConditionContext.class,0);
		}
		public OredConditionContext oredCondition() {
			return getRuleContext(OredConditionContext.class,0);
		}
		public JoinConditionContext joinCondition() {
			return getRuleContext(JoinConditionContext.class,0);
		}
		public SimpleConditionContext simpleCondition() {
			return getRuleContext(SimpleConditionContext.class,0);
		}
		public ConditionContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_condition; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof PredicateListener ) ((PredicateListener)listener).enterCondition(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof PredicateListener ) ((PredicateListener)listener).exitCondition(this);
		}
	}

	public final ConditionContext condition() throws RecognitionException {
		ConditionContext _localctx = new ConditionContext(_ctx, getState());
		enterRule(_localctx, 4, RULE_condition);
		try {
			setState(46);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,0,_ctx) ) {
			case 1:
				enterOuterAlt(_localctx, 1);
				{
				setState(42);
				andedCondition();
				}
				break;
			case 2:
				enterOuterAlt(_localctx, 2);
				{
				setState(43);
				oredCondition();
				}
				break;
			case 3:
				enterOuterAlt(_localctx, 3);
				{
				setState(44);
				joinCondition();
				}
				break;
			case 4:
				enterOuterAlt(_localctx, 4);
				{
				setState(45);
				simpleCondition();
				}
				break;
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class AndedConditionContext extends ParserRuleContext {
		public List<ConditionStrContext> conditionStr() {
			return getRuleContexts(ConditionStrContext.class);
		}
		public ConditionStrContext conditionStr(int i) {
			return getRuleContext(ConditionStrContext.class,i);
		}
		public List<AndContext> and() {
			return getRuleContexts(AndContext.class);
		}
		public AndContext and(int i) {
			return getRuleContext(AndContext.class,i);
		}
		public AndedConditionContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_andedCondition; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof PredicateListener ) ((PredicateListener)listener).enterAndedCondition(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof PredicateListener ) ((PredicateListener)listener).exitAndedCondition(this);
		}
	}

	public final AndedConditionContext andedCondition() throws RecognitionException {
		AndedConditionContext _localctx = new AndedConditionContext(_ctx, getState());
		enterRule(_localctx, 6, RULE_andedCondition);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(48);
			conditionStr();
			setState(52); 
			_errHandler.sync(this);
			_la = _input.LA(1);
			do {
				{
				{
				setState(49);
				and();
				setState(50);
				conditionStr();
				}
				}
				setState(54); 
				_errHandler.sync(this);
				_la = _input.LA(1);
			} while ( _la==SPACE );
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class OredConditionContext extends ParserRuleContext {
		public List<ConditionStrContext> conditionStr() {
			return getRuleContexts(ConditionStrContext.class);
		}
		public ConditionStrContext conditionStr(int i) {
			return getRuleContext(ConditionStrContext.class,i);
		}
		public List<OrContext> or() {
			return getRuleContexts(OrContext.class);
		}
		public OrContext or(int i) {
			return getRuleContext(OrContext.class,i);
		}
		public OredConditionContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_oredCondition; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof PredicateListener ) ((PredicateListener)listener).enterOredCondition(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof PredicateListener ) ((PredicateListener)listener).exitOredCondition(this);
		}
	}

	public final OredConditionContext oredCondition() throws RecognitionException {
		OredConditionContext _localctx = new OredConditionContext(_ctx, getState());
		enterRule(_localctx, 8, RULE_oredCondition);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(56);
			conditionStr();
			setState(60); 
			_errHandler.sync(this);
			_la = _input.LA(1);
			do {
				{
				{
				setState(57);
				or();
				setState(58);
				conditionStr();
				}
				}
				setState(62); 
				_errHandler.sync(this);
				_la = _input.LA(1);
			} while ( _la==SPACE );
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class JoinConditionContext extends ParserRuleContext {
		public List<ColumnnameContext> columnname() {
			return getRuleContexts(ColumnnameContext.class);
		}
		public ColumnnameContext columnname(int i) {
			return getRuleContext(ColumnnameContext.class,i);
		}
		public List<TerminalNode> SPACE() { return getTokens(PredicateParser.SPACE); }
		public TerminalNode SPACE(int i) {
			return getToken(PredicateParser.SPACE, i);
		}
		public JoinConditionContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_joinCondition; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof PredicateListener ) ((PredicateListener)listener).enterJoinCondition(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof PredicateListener ) ((PredicateListener)listener).exitJoinCondition(this);
		}
	}

	public final JoinConditionContext joinCondition() throws RecognitionException {
		JoinConditionContext _localctx = new JoinConditionContext(_ctx, getState());
		enterRule(_localctx, 10, RULE_joinCondition);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(64);
			columnname();
			setState(65);
			match(SPACE);
			setState(66);
			match(T__0);
			setState(67);
			match(SPACE);
			setState(68);
			columnname();
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class SimpleConditionContext extends ParserRuleContext {
		public SimpleNumConditionContext simpleNumCondition() {
			return getRuleContext(SimpleNumConditionContext.class,0);
		}
		public SimpleStrConditionContext simpleStrCondition() {
			return getRuleContext(SimpleStrConditionContext.class,0);
		}
		public SimpleDateConditionContext simpleDateCondition() {
			return getRuleContext(SimpleDateConditionContext.class,0);
		}
		public SimpleINConditionContext simpleINCondition() {
			return getRuleContext(SimpleINConditionContext.class,0);
		}
		public SimpleConditionContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_simpleCondition; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof PredicateListener ) ((PredicateListener)listener).enterSimpleCondition(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof PredicateListener ) ((PredicateListener)listener).exitSimpleCondition(this);
		}
	}

	public final SimpleConditionContext simpleCondition() throws RecognitionException {
		SimpleConditionContext _localctx = new SimpleConditionContext(_ctx, getState());
		enterRule(_localctx, 12, RULE_simpleCondition);
		try {
			setState(74);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,3,_ctx) ) {
			case 1:
				enterOuterAlt(_localctx, 1);
				{
				setState(70);
				simpleNumCondition();
				}
				break;
			case 2:
				enterOuterAlt(_localctx, 2);
				{
				setState(71);
				simpleStrCondition();
				}
				break;
			case 3:
				enterOuterAlt(_localctx, 3);
				{
				setState(72);
				simpleDateCondition();
				}
				break;
			case 4:
				enterOuterAlt(_localctx, 4);
				{
				setState(73);
				simpleINCondition();
				}
				break;
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class SimpleNumConditionContext extends ParserRuleContext {
		public ColumnnameContext columnname() {
			return getRuleContext(ColumnnameContext.class,0);
		}
		public List<TerminalNode> SPACE() { return getTokens(PredicateParser.SPACE); }
		public TerminalNode SPACE(int i) {
			return getToken(PredicateParser.SPACE, i);
		}
		public SymbolContext symbol() {
			return getRuleContext(SymbolContext.class,0);
		}
		public NumberContext number() {
			return getRuleContext(NumberContext.class,0);
		}
		public TerminalNode OPEN() { return getToken(PredicateParser.OPEN, 0); }
		public TerminalNode CLOSE() { return getToken(PredicateParser.CLOSE, 0); }
		public SimpleNumConditionContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_simpleNumCondition; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof PredicateListener ) ((PredicateListener)listener).enterSimpleNumCondition(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof PredicateListener ) ((PredicateListener)listener).exitSimpleNumCondition(this);
		}
	}

	public final SimpleNumConditionContext simpleNumCondition() throws RecognitionException {
		SimpleNumConditionContext _localctx = new SimpleNumConditionContext(_ctx, getState());
		enterRule(_localctx, 14, RULE_simpleNumCondition);
		int _la;
		try {
			setState(94);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,6,_ctx) ) {
			case 1:
				enterOuterAlt(_localctx, 1);
				{
				setState(76);
				columnname();
				setState(77);
				match(SPACE);
				setState(78);
				symbol();
				setState(79);
				match(SPACE);
				setState(80);
				number();
				setState(82);
				_la = _input.LA(1);
				if (_la==T__1) {
					{
					setState(81);
					match(T__1);
					}
				}

				}
				break;
			case 2:
				enterOuterAlt(_localctx, 2);
				{
				setState(84);
				columnname();
				setState(85);
				match(SPACE);
				setState(86);
				symbol();
				setState(87);
				match(SPACE);
				setState(88);
				match(OPEN);
				setState(89);
				number();
				setState(90);
				match(CLOSE);
				setState(92);
				_la = _input.LA(1);
				if (_la==T__1) {
					{
					setState(91);
					match(T__1);
					}
				}

				}
				break;
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class SimpleStrConditionContext extends ParserRuleContext {
		public TerminalNode OPEN() { return getToken(PredicateParser.OPEN, 0); }
		public ColumnnameContext columnname() {
			return getRuleContext(ColumnnameContext.class,0);
		}
		public TerminalNode CLOSE() { return getToken(PredicateParser.CLOSE, 0); }
		public List<TerminalNode> SPACE() { return getTokens(PredicateParser.SPACE); }
		public TerminalNode SPACE(int i) {
			return getToken(PredicateParser.SPACE, i);
		}
		public SymbolContext symbol() {
			return getRuleContext(SymbolContext.class,0);
		}
		public List<TerminalNode> QUOTE() { return getTokens(PredicateParser.QUOTE); }
		public TerminalNode QUOTE(int i) {
			return getToken(PredicateParser.QUOTE, i);
		}
		public StringliteralContext stringliteral() {
			return getRuleContext(StringliteralContext.class,0);
		}
		public TerminalNode INTEGER() { return getToken(PredicateParser.INTEGER, 0); }
		public TerminalNode DECIMAL() { return getToken(PredicateParser.DECIMAL, 0); }
		public SimpleStrConditionContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_simpleStrCondition; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof PredicateListener ) ((PredicateListener)listener).enterSimpleStrCondition(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof PredicateListener ) ((PredicateListener)listener).exitSimpleStrCondition(this);
		}
	}

	public final SimpleStrConditionContext simpleStrCondition() throws RecognitionException {
		SimpleStrConditionContext _localctx = new SimpleStrConditionContext(_ctx, getState());
		enterRule(_localctx, 16, RULE_simpleStrCondition);
		try {
			setState(148);
			switch (_input.LA(1)) {
			case OPEN:
				enterOuterAlt(_localctx, 1);
				{
				setState(96);
				match(OPEN);
				setState(97);
				columnname();
				setState(98);
				match(CLOSE);
				setState(99);
				match(T__2);
				setState(100);
				match(SPACE);
				setState(101);
				symbol();
				setState(102);
				match(SPACE);
				setState(103);
				match(QUOTE);
				setState(104);
				stringliteral();
				setState(105);
				match(QUOTE);
				setState(106);
				match(T__2);
				}
				break;
			case TEXT:
				enterOuterAlt(_localctx, 2);
				{
				setState(108);
				columnname();
				setState(109);
				match(SPACE);
				setState(110);
				symbol();
				setState(111);
				match(SPACE);
				setState(146);
				_errHandler.sync(this);
				switch ( getInterpreter().adaptivePredict(_input,7,_ctx) ) {
				case 1:
					{
					setState(112);
					match(QUOTE);
					setState(113);
					stringliteral();
					setState(114);
					match(QUOTE);
					}
					break;
				case 2:
					{
					setState(116);
					match(QUOTE);
					setState(117);
					stringliteral();
					setState(118);
					match(QUOTE);
					setState(119);
					match(T__3);
					}
					break;
				case 3:
					{
					setState(121);
					match(QUOTE);
					setState(122);
					stringliteral();
					setState(123);
					match(QUOTE);
					setState(124);
					match(T__2);
					}
					break;
				case 4:
					{
					setState(126);
					match(QUOTE);
					setState(127);
					match(INTEGER);
					setState(128);
					match(QUOTE);
					setState(129);
					match(T__1);
					}
					break;
				case 5:
					{
					setState(130);
					match(OPEN);
					setState(131);
					match(INTEGER);
					setState(132);
					match(CLOSE);
					setState(133);
					match(T__1);
					}
					break;
				case 6:
					{
					setState(134);
					match(QUOTE);
					setState(135);
					match(INTEGER);
					setState(136);
					match(QUOTE);
					setState(137);
					match(T__4);
					}
					break;
				case 7:
					{
					setState(138);
					match(OPEN);
					setState(139);
					match(INTEGER);
					setState(140);
					match(CLOSE);
					setState(141);
					match(T__4);
					}
					break;
				case 8:
					{
					setState(142);
					match(QUOTE);
					setState(143);
					match(DECIMAL);
					setState(144);
					match(QUOTE);
					setState(145);
					match(T__2);
					}
					break;
				}
				}
				break;
			default:
				throw new NoViableAltException(this);
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class SimpleDateConditionContext extends ParserRuleContext {
		public ColumnnameContext columnname() {
			return getRuleContext(ColumnnameContext.class,0);
		}
		public List<TerminalNode> SPACE() { return getTokens(PredicateParser.SPACE); }
		public TerminalNode SPACE(int i) {
			return getToken(PredicateParser.SPACE, i);
		}
		public SymbolContext symbol() {
			return getRuleContext(SymbolContext.class,0);
		}
		public List<TerminalNode> QUOTE() { return getTokens(PredicateParser.QUOTE); }
		public TerminalNode QUOTE(int i) {
			return getToken(PredicateParser.QUOTE, i);
		}
		public TerminalNode DATE() { return getToken(PredicateParser.DATE, 0); }
		public TerminalNode TIMESTAMP() { return getToken(PredicateParser.TIMESTAMP, 0); }
		public SimpleDateConditionContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_simpleDateCondition; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof PredicateListener ) ((PredicateListener)listener).enterSimpleDateCondition(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof PredicateListener ) ((PredicateListener)listener).exitSimpleDateCondition(this);
		}
	}

	public final SimpleDateConditionContext simpleDateCondition() throws RecognitionException {
		SimpleDateConditionContext _localctx = new SimpleDateConditionContext(_ctx, getState());
		enterRule(_localctx, 18, RULE_simpleDateCondition);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(150);
			columnname();
			setState(151);
			match(SPACE);
			setState(152);
			symbol();
			setState(153);
			match(SPACE);
			setState(162);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,9,_ctx) ) {
			case 1:
				{
				setState(154);
				match(QUOTE);
				setState(155);
				match(DATE);
				setState(156);
				match(QUOTE);
				setState(157);
				match(T__5);
				}
				break;
			case 2:
				{
				setState(158);
				match(QUOTE);
				setState(159);
				match(TIMESTAMP);
				setState(160);
				match(QUOTE);
				setState(161);
				match(T__6);
				}
				break;
			}
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class SimpleINConditionContext extends ParserRuleContext {
		public List<TerminalNode> OPEN() { return getTokens(PredicateParser.OPEN); }
		public TerminalNode OPEN(int i) {
			return getToken(PredicateParser.OPEN, i);
		}
		public ColumnnameContext columnname() {
			return getRuleContext(ColumnnameContext.class,0);
		}
		public List<TerminalNode> CLOSE() { return getTokens(PredicateParser.CLOSE); }
		public TerminalNode CLOSE(int i) {
			return getToken(PredicateParser.CLOSE, i);
		}
		public List<TerminalNode> SPACE() { return getTokens(PredicateParser.SPACE); }
		public TerminalNode SPACE(int i) {
			return getToken(PredicateParser.SPACE, i);
		}
		public AnyContext any() {
			return getRuleContext(AnyContext.class,0);
		}
		public List<TerminalNode> QUOTE() { return getTokens(PredicateParser.QUOTE); }
		public TerminalNode QUOTE(int i) {
			return getToken(PredicateParser.QUOTE, i);
		}
		public List<StringliteralContext> stringliteral() {
			return getRuleContexts(StringliteralContext.class);
		}
		public StringliteralContext stringliteral(int i) {
			return getRuleContext(StringliteralContext.class,i);
		}
		public List<TerminalNode> INTEGER() { return getTokens(PredicateParser.INTEGER); }
		public TerminalNode INTEGER(int i) {
			return getToken(PredicateParser.INTEGER, i);
		}
		public SimpleINConditionContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_simpleINCondition; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof PredicateListener ) ((PredicateListener)listener).enterSimpleINCondition(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof PredicateListener ) ((PredicateListener)listener).exitSimpleINCondition(this);
		}
	}

	public final SimpleINConditionContext simpleINCondition() throws RecognitionException {
		SimpleINConditionContext _localctx = new SimpleINConditionContext(_ctx, getState());
		enterRule(_localctx, 20, RULE_simpleINCondition);
		int _la;
		try {
			setState(252);
			switch (_input.LA(1)) {
			case OPEN:
				enterOuterAlt(_localctx, 1);
				{
				setState(164);
				match(OPEN);
				setState(165);
				columnname();
				setState(166);
				match(CLOSE);
				setState(167);
				match(T__2);
				setState(168);
				match(SPACE);
				setState(169);
				match(T__0);
				setState(170);
				match(SPACE);
				setState(171);
				any();
				setState(172);
				match(OPEN);
				setState(173);
				match(QUOTE);
				setState(174);
				match(T__7);
				setState(175);
				stringliteral();
				setState(180);
				_errHandler.sync(this);
				_la = _input.LA(1);
				while (_la==T__8) {
					{
					{
					setState(176);
					match(T__8);
					setState(177);
					stringliteral();
					}
					}
					setState(182);
					_errHandler.sync(this);
					_la = _input.LA(1);
				}
				setState(183);
				match(T__9);
				setState(184);
				match(QUOTE);
				setState(185);
				match(T__10);
				setState(186);
				match(CLOSE);
				}
				break;
			case TEXT:
				enterOuterAlt(_localctx, 2);
				{
				setState(188);
				columnname();
				setState(189);
				match(SPACE);
				setState(190);
				match(T__0);
				setState(191);
				match(SPACE);
				setState(192);
				any();
				setState(193);
				match(OPEN);
				setState(248);
				_errHandler.sync(this);
				switch ( getInterpreter().adaptivePredict(_input,15,_ctx) ) {
				case 1:
					{
					setState(194);
					match(QUOTE);
					setState(195);
					match(T__7);
					setState(196);
					stringliteral();
					setState(201);
					_errHandler.sync(this);
					_la = _input.LA(1);
					while (_la==T__8) {
						{
						{
						setState(197);
						match(T__8);
						setState(198);
						stringliteral();
						}
						}
						setState(203);
						_errHandler.sync(this);
						_la = _input.LA(1);
					}
					setState(204);
					match(T__9);
					setState(205);
					match(QUOTE);
					setState(206);
					match(T__11);
					}
					break;
				case 2:
					{
					setState(208);
					match(QUOTE);
					setState(209);
					match(T__7);
					setState(210);
					stringliteral();
					setState(215);
					_errHandler.sync(this);
					_la = _input.LA(1);
					while (_la==T__8) {
						{
						{
						setState(211);
						match(T__8);
						setState(212);
						stringliteral();
						}
						}
						setState(217);
						_errHandler.sync(this);
						_la = _input.LA(1);
					}
					setState(218);
					match(T__9);
					setState(219);
					match(QUOTE);
					setState(220);
					match(T__10);
					}
					break;
				case 3:
					{
					setState(222);
					match(QUOTE);
					setState(223);
					match(T__7);
					setState(224);
					match(INTEGER);
					setState(229);
					_errHandler.sync(this);
					_la = _input.LA(1);
					while (_la==T__8) {
						{
						{
						setState(225);
						match(T__8);
						setState(226);
						match(INTEGER);
						}
						}
						setState(231);
						_errHandler.sync(this);
						_la = _input.LA(1);
					}
					setState(232);
					match(T__9);
					setState(233);
					match(QUOTE);
					setState(234);
					match(T__12);
					}
					break;
				case 4:
					{
					setState(235);
					match(QUOTE);
					setState(236);
					match(T__7);
					setState(237);
					match(INTEGER);
					setState(242);
					_errHandler.sync(this);
					_la = _input.LA(1);
					while (_la==T__8) {
						{
						{
						setState(238);
						match(T__8);
						setState(239);
						match(INTEGER);
						}
						}
						setState(244);
						_errHandler.sync(this);
						_la = _input.LA(1);
					}
					setState(245);
					match(T__9);
					setState(246);
					match(QUOTE);
					setState(247);
					match(T__13);
					}
					break;
				}
				setState(250);
				match(CLOSE);
				}
				break;
			default:
				throw new NoViableAltException(this);
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class AndContext extends ParserRuleContext {
		public List<TerminalNode> SPACE() { return getTokens(PredicateParser.SPACE); }
		public TerminalNode SPACE(int i) {
			return getToken(PredicateParser.SPACE, i);
		}
		public AndContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_and; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof PredicateListener ) ((PredicateListener)listener).enterAnd(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof PredicateListener ) ((PredicateListener)listener).exitAnd(this);
		}
	}

	public final AndContext and() throws RecognitionException {
		AndContext _localctx = new AndContext(_ctx, getState());
		enterRule(_localctx, 22, RULE_and);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(254);
			match(SPACE);
			setState(255);
			match(T__14);
			setState(256);
			match(SPACE);
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class OrContext extends ParserRuleContext {
		public List<TerminalNode> SPACE() { return getTokens(PredicateParser.SPACE); }
		public TerminalNode SPACE(int i) {
			return getToken(PredicateParser.SPACE, i);
		}
		public OrContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_or; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof PredicateListener ) ((PredicateListener)listener).enterOr(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof PredicateListener ) ((PredicateListener)listener).exitOr(this);
		}
	}

	public final OrContext or() throws RecognitionException {
		OrContext _localctx = new OrContext(_ctx, getState());
		enterRule(_localctx, 24, RULE_or);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(258);
			match(SPACE);
			setState(259);
			match(T__15);
			setState(260);
			match(SPACE);
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class AnyContext extends ParserRuleContext {
		public TerminalNode SPACE() { return getToken(PredicateParser.SPACE, 0); }
		public AnyContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_any; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof PredicateListener ) ((PredicateListener)listener).enterAny(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof PredicateListener ) ((PredicateListener)listener).exitAny(this);
		}
	}

	public final AnyContext any() throws RecognitionException {
		AnyContext _localctx = new AnyContext(_ctx, getState());
		enterRule(_localctx, 26, RULE_any);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(262);
			match(T__16);
			setState(263);
			match(SPACE);
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class ColumnnameContext extends ParserRuleContext {
		public List<TerminalNode> TEXT() { return getTokens(PredicateParser.TEXT); }
		public TerminalNode TEXT(int i) {
			return getToken(PredicateParser.TEXT, i);
		}
		public TerminalNode DOT() { return getToken(PredicateParser.DOT, 0); }
		public ColumnnameContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_columnname; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof PredicateListener ) ((PredicateListener)listener).enterColumnname(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof PredicateListener ) ((PredicateListener)listener).exitColumnname(this);
		}
	}

	public final ColumnnameContext columnname() throws RecognitionException {
		ColumnnameContext _localctx = new ColumnnameContext(_ctx, getState());
		enterRule(_localctx, 28, RULE_columnname);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(265);
			match(TEXT);
			setState(268);
			_la = _input.LA(1);
			if (_la==DOT) {
				{
				setState(266);
				match(DOT);
				setState(267);
				match(TEXT);
				}
			}

			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class SymbolContext extends ParserRuleContext {
		public SymbolContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_symbol; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof PredicateListener ) ((PredicateListener)listener).enterSymbol(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof PredicateListener ) ((PredicateListener)listener).exitSymbol(this);
		}
	}

	public final SymbolContext symbol() throws RecognitionException {
		SymbolContext _localctx = new SymbolContext(_ctx, getState());
		enterRule(_localctx, 30, RULE_symbol);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(270);
			_la = _input.LA(1);
			if ( !((((_la) & ~0x3f) == 0 && ((1L << _la) & ((1L << T__0) | (1L << T__17) | (1L << T__18) | (1L << T__19) | (1L << T__20))) != 0)) ) {
			_errHandler.recoverInline(this);
			} else {
				consume();
			}
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class NumberContext extends ParserRuleContext {
		public TerminalNode INTEGER() { return getToken(PredicateParser.INTEGER, 0); }
		public TerminalNode DECIMAL() { return getToken(PredicateParser.DECIMAL, 0); }
		public NumberContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_number; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof PredicateListener ) ((PredicateListener)listener).enterNumber(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof PredicateListener ) ((PredicateListener)listener).exitNumber(this);
		}
	}

	public final NumberContext number() throws RecognitionException {
		NumberContext _localctx = new NumberContext(_ctx, getState());
		enterRule(_localctx, 32, RULE_number);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(272);
			_la = _input.LA(1);
			if ( !(_la==INTEGER || _la==DECIMAL) ) {
			_errHandler.recoverInline(this);
			} else {
				consume();
			}
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class StringliteralContext extends ParserRuleContext {
		public List<TerminalNode> TEXT() { return getTokens(PredicateParser.TEXT); }
		public TerminalNode TEXT(int i) {
			return getToken(PredicateParser.TEXT, i);
		}
		public List<TerminalNode> OPEN() { return getTokens(PredicateParser.OPEN); }
		public TerminalNode OPEN(int i) {
			return getToken(PredicateParser.OPEN, i);
		}
		public List<TerminalNode> CLOSE() { return getTokens(PredicateParser.CLOSE); }
		public TerminalNode CLOSE(int i) {
			return getToken(PredicateParser.CLOSE, i);
		}
		public List<TerminalNode> SPACE() { return getTokens(PredicateParser.SPACE); }
		public TerminalNode SPACE(int i) {
			return getToken(PredicateParser.SPACE, i);
		}
		public List<TerminalNode> INTEGER() { return getTokens(PredicateParser.INTEGER); }
		public TerminalNode INTEGER(int i) {
			return getToken(PredicateParser.INTEGER, i);
		}
		public StringliteralContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_stringliteral; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof PredicateListener ) ((PredicateListener)listener).enterStringliteral(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof PredicateListener ) ((PredicateListener)listener).exitStringliteral(this);
		}
	}

	public final StringliteralContext stringliteral() throws RecognitionException {
		StringliteralContext _localctx = new StringliteralContext(_ctx, getState());
		enterRule(_localctx, 34, RULE_stringliteral);
		int _la;
		try {
			int _alt;
			setState(291);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,20,_ctx) ) {
			case 1:
				enterOuterAlt(_localctx, 1);
				{
				setState(274);
				match(TEXT);
				}
				break;
			case 2:
				enterOuterAlt(_localctx, 2);
				{
				setState(275);
				match(OPEN);
				setState(276);
				match(TEXT);
				setState(277);
				match(CLOSE);
				}
				break;
			case 3:
				enterOuterAlt(_localctx, 3);
				{
				setState(281);
				_errHandler.sync(this);
				_alt = getInterpreter().adaptivePredict(_input,18,_ctx);
				while ( _alt!=2 && _alt!=org.antlr.v4.runtime.atn.ATN.INVALID_ALT_NUMBER ) {
					if ( _alt==1 ) {
						{
						{
						setState(278);
						_la = _input.LA(1);
						if ( !((((_la) & ~0x3f) == 0 && ((1L << _la) & ((1L << T__21) | (1L << T__22) | (1L << T__23) | (1L << T__24) | (1L << OPEN) | (1L << CLOSE) | (1L << TEXT) | (1L << SPACE) | (1L << INTEGER))) != 0)) ) {
						_errHandler.recoverInline(this);
						} else {
							consume();
						}
						}
						} 
					}
					setState(283);
					_errHandler.sync(this);
					_alt = getInterpreter().adaptivePredict(_input,18,_ctx);
				}
				{
				setState(284);
				match(SPACE);
				}
				setState(288);
				_errHandler.sync(this);
				_la = _input.LA(1);
				while ((((_la) & ~0x3f) == 0 && ((1L << _la) & ((1L << T__21) | (1L << T__22) | (1L << T__23) | (1L << T__24) | (1L << OPEN) | (1L << CLOSE) | (1L << TEXT) | (1L << SPACE) | (1L << INTEGER))) != 0)) {
					{
					{
					setState(285);
					_la = _input.LA(1);
					if ( !((((_la) & ~0x3f) == 0 && ((1L << _la) & ((1L << T__21) | (1L << T__22) | (1L << T__23) | (1L << T__24) | (1L << OPEN) | (1L << CLOSE) | (1L << TEXT) | (1L << SPACE) | (1L << INTEGER))) != 0)) ) {
					_errHandler.recoverInline(this);
					} else {
						consume();
					}
					}
					}
					setState(290);
					_errHandler.sync(this);
					_la = _input.LA(1);
				}
				}
				break;
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static final String _serializedATN =
		"\3\u0430\ud6d1\u8206\uad2d\u4417\uaef1\u8d80\uaadd\3%\u0128\4\2\t\2\4"+
		"\3\t\3\4\4\t\4\4\5\t\5\4\6\t\6\4\7\t\7\4\b\t\b\4\t\t\t\4\n\t\n\4\13\t"+
		"\13\4\f\t\f\4\r\t\r\4\16\t\16\4\17\t\17\4\20\t\20\4\21\t\21\4\22\t\22"+
		"\4\23\t\23\3\2\3\2\3\3\3\3\3\3\3\3\3\4\3\4\3\4\3\4\5\4\61\n\4\3\5\3\5"+
		"\3\5\3\5\6\5\67\n\5\r\5\16\58\3\6\3\6\3\6\3\6\6\6?\n\6\r\6\16\6@\3\7\3"+
		"\7\3\7\3\7\3\7\3\7\3\b\3\b\3\b\3\b\5\bM\n\b\3\t\3\t\3\t\3\t\3\t\3\t\5"+
		"\tU\n\t\3\t\3\t\3\t\3\t\3\t\3\t\3\t\3\t\5\t_\n\t\5\ta\n\t\3\n\3\n\3\n"+
		"\3\n\3\n\3\n\3\n\3\n\3\n\3\n\3\n\3\n\3\n\3\n\3\n\3\n\3\n\3\n\3\n\3\n\3"+
		"\n\3\n\3\n\3\n\3\n\3\n\3\n\3\n\3\n\3\n\3\n\3\n\3\n\3\n\3\n\3\n\3\n\3\n"+
		"\3\n\3\n\3\n\3\n\3\n\3\n\3\n\3\n\3\n\3\n\3\n\3\n\5\n\u0095\n\n\5\n\u0097"+
		"\n\n\3\13\3\13\3\13\3\13\3\13\3\13\3\13\3\13\3\13\3\13\3\13\3\13\5\13"+
		"\u00a5\n\13\3\f\3\f\3\f\3\f\3\f\3\f\3\f\3\f\3\f\3\f\3\f\3\f\3\f\3\f\7"+
		"\f\u00b5\n\f\f\f\16\f\u00b8\13\f\3\f\3\f\3\f\3\f\3\f\3\f\3\f\3\f\3\f\3"+
		"\f\3\f\3\f\3\f\3\f\3\f\3\f\7\f\u00ca\n\f\f\f\16\f\u00cd\13\f\3\f\3\f\3"+
		"\f\3\f\3\f\3\f\3\f\3\f\3\f\7\f\u00d8\n\f\f\f\16\f\u00db\13\f\3\f\3\f\3"+
		"\f\3\f\3\f\3\f\3\f\3\f\3\f\7\f\u00e6\n\f\f\f\16\f\u00e9\13\f\3\f\3\f\3"+
		"\f\3\f\3\f\3\f\3\f\3\f\7\f\u00f3\n\f\f\f\16\f\u00f6\13\f\3\f\3\f\3\f\5"+
		"\f\u00fb\n\f\3\f\3\f\5\f\u00ff\n\f\3\r\3\r\3\r\3\r\3\16\3\16\3\16\3\16"+
		"\3\17\3\17\3\17\3\20\3\20\3\20\5\20\u010f\n\20\3\21\3\21\3\22\3\22\3\23"+
		"\3\23\3\23\3\23\3\23\7\23\u011a\n\23\f\23\16\23\u011d\13\23\3\23\3\23"+
		"\7\23\u0121\n\23\f\23\16\23\u0124\13\23\5\23\u0126\n\23\3\23\2\2\24\2"+
		"\4\6\b\n\f\16\20\22\24\26\30\32\34\36 \"$\2\5\4\2\3\3\24\27\3\2\"#\4\2"+
		"\30\35 \"\u0137\2&\3\2\2\2\4(\3\2\2\2\6\60\3\2\2\2\b\62\3\2\2\2\n:\3\2"+
		"\2\2\fB\3\2\2\2\16L\3\2\2\2\20`\3\2\2\2\22\u0096\3\2\2\2\24\u0098\3\2"+
		"\2\2\26\u00fe\3\2\2\2\30\u0100\3\2\2\2\32\u0104\3\2\2\2\34\u0108\3\2\2"+
		"\2\36\u010b\3\2\2\2 \u0110\3\2\2\2\"\u0112\3\2\2\2$\u0125\3\2\2\2&\'\5"+
		"\4\3\2\'\3\3\2\2\2()\7\34\2\2)*\5\6\4\2*+\7\35\2\2+\5\3\2\2\2,\61\5\b"+
		"\5\2-\61\5\n\6\2.\61\5\f\7\2/\61\5\16\b\2\60,\3\2\2\2\60-\3\2\2\2\60."+
		"\3\2\2\2\60/\3\2\2\2\61\7\3\2\2\2\62\66\5\4\3\2\63\64\5\30\r\2\64\65\5"+
		"\4\3\2\65\67\3\2\2\2\66\63\3\2\2\2\678\3\2\2\28\66\3\2\2\289\3\2\2\29"+
		"\t\3\2\2\2:>\5\4\3\2;<\5\32\16\2<=\5\4\3\2=?\3\2\2\2>;\3\2\2\2?@\3\2\2"+
		"\2@>\3\2\2\2@A\3\2\2\2A\13\3\2\2\2BC\5\36\20\2CD\7!\2\2DE\7\3\2\2EF\7"+
		"!\2\2FG\5\36\20\2G\r\3\2\2\2HM\5\20\t\2IM\5\22\n\2JM\5\24\13\2KM\5\26"+
		"\f\2LH\3\2\2\2LI\3\2\2\2LJ\3\2\2\2LK\3\2\2\2M\17\3\2\2\2NO\5\36\20\2O"+
		"P\7!\2\2PQ\5 \21\2QR\7!\2\2RT\5\"\22\2SU\7\4\2\2TS\3\2\2\2TU\3\2\2\2U"+
		"a\3\2\2\2VW\5\36\20\2WX\7!\2\2XY\5 \21\2YZ\7!\2\2Z[\7\34\2\2[\\\5\"\22"+
		"\2\\^\7\35\2\2]_\7\4\2\2^]\3\2\2\2^_\3\2\2\2_a\3\2\2\2`N\3\2\2\2`V\3\2"+
		"\2\2a\21\3\2\2\2bc\7\34\2\2cd\5\36\20\2de\7\35\2\2ef\7\5\2\2fg\7!\2\2"+
		"gh\5 \21\2hi\7!\2\2ij\7\36\2\2jk\5$\23\2kl\7\36\2\2lm\7\5\2\2m\u0097\3"+
		"\2\2\2no\5\36\20\2op\7!\2\2pq\5 \21\2q\u0094\7!\2\2rs\7\36\2\2st\5$\23"+
		"\2tu\7\36\2\2u\u0095\3\2\2\2vw\7\36\2\2wx\5$\23\2xy\7\36\2\2yz\7\6\2\2"+
		"z\u0095\3\2\2\2{|\7\36\2\2|}\5$\23\2}~\7\36\2\2~\177\7\5\2\2\177\u0095"+
		"\3\2\2\2\u0080\u0081\7\36\2\2\u0081\u0082\7\"\2\2\u0082\u0083\7\36\2\2"+
		"\u0083\u0095\7\4\2\2\u0084\u0085\7\34\2\2\u0085\u0086\7\"\2\2\u0086\u0087"+
		"\7\35\2\2\u0087\u0095\7\4\2\2\u0088\u0089\7\36\2\2\u0089\u008a\7\"\2\2"+
		"\u008a\u008b\7\36\2\2\u008b\u0095\7\7\2\2\u008c\u008d\7\34\2\2\u008d\u008e"+
		"\7\"\2\2\u008e\u008f\7\35\2\2\u008f\u0095\7\7\2\2\u0090\u0091\7\36\2\2"+
		"\u0091\u0092\7#\2\2\u0092\u0093\7\36\2\2\u0093\u0095\7\5\2\2\u0094r\3"+
		"\2\2\2\u0094v\3\2\2\2\u0094{\3\2\2\2\u0094\u0080\3\2\2\2\u0094\u0084\3"+
		"\2\2\2\u0094\u0088\3\2\2\2\u0094\u008c\3\2\2\2\u0094\u0090\3\2\2\2\u0095"+
		"\u0097\3\2\2\2\u0096b\3\2\2\2\u0096n\3\2\2\2\u0097\23\3\2\2\2\u0098\u0099"+
		"\5\36\20\2\u0099\u009a\7!\2\2\u009a\u009b\5 \21\2\u009b\u00a4\7!\2\2\u009c"+
		"\u009d\7\36\2\2\u009d\u009e\7$\2\2\u009e\u009f\7\36\2\2\u009f\u00a5\7"+
		"\b\2\2\u00a0\u00a1\7\36\2\2\u00a1\u00a2\7%\2\2\u00a2\u00a3\7\36\2\2\u00a3"+
		"\u00a5\7\t\2\2\u00a4\u009c\3\2\2\2\u00a4\u00a0\3\2\2\2\u00a5\25\3\2\2"+
		"\2\u00a6\u00a7\7\34\2\2\u00a7\u00a8\5\36\20\2\u00a8\u00a9\7\35\2\2\u00a9"+
		"\u00aa\7\5\2\2\u00aa\u00ab\7!\2\2\u00ab\u00ac\7\3\2\2\u00ac\u00ad\7!\2"+
		"\2\u00ad\u00ae\5\34\17\2\u00ae\u00af\7\34\2\2\u00af\u00b0\7\36\2\2\u00b0"+
		"\u00b1\7\n\2\2\u00b1\u00b6\5$\23\2\u00b2\u00b3\7\13\2\2\u00b3\u00b5\5"+
		"$\23\2\u00b4\u00b2\3\2\2\2\u00b5\u00b8\3\2\2\2\u00b6\u00b4\3\2\2\2\u00b6"+
		"\u00b7\3\2\2\2\u00b7\u00b9\3\2\2\2\u00b8\u00b6\3\2\2\2\u00b9\u00ba\7\f"+
		"\2\2\u00ba\u00bb\7\36\2\2\u00bb\u00bc\7\r\2\2\u00bc\u00bd\7\35\2\2\u00bd"+
		"\u00ff\3\2\2\2\u00be\u00bf\5\36\20\2\u00bf\u00c0\7!\2\2\u00c0\u00c1\7"+
		"\3\2\2\u00c1\u00c2\7!\2\2\u00c2\u00c3\5\34\17\2\u00c3\u00fa\7\34\2\2\u00c4"+
		"\u00c5\7\36\2\2\u00c5\u00c6\7\n\2\2\u00c6\u00cb\5$\23\2\u00c7\u00c8\7"+
		"\13\2\2\u00c8\u00ca\5$\23\2\u00c9\u00c7\3\2\2\2\u00ca\u00cd\3\2\2\2\u00cb"+
		"\u00c9\3\2\2\2\u00cb\u00cc\3\2\2\2\u00cc\u00ce\3\2\2\2\u00cd\u00cb\3\2"+
		"\2\2\u00ce\u00cf\7\f\2\2\u00cf\u00d0\7\36\2\2\u00d0\u00d1\7\16\2\2\u00d1"+
		"\u00fb\3\2\2\2\u00d2\u00d3\7\36\2\2\u00d3\u00d4\7\n\2\2\u00d4\u00d9\5"+
		"$\23\2\u00d5\u00d6\7\13\2\2\u00d6\u00d8\5$\23\2\u00d7\u00d5\3\2\2\2\u00d8"+
		"\u00db\3\2\2\2\u00d9\u00d7\3\2\2\2\u00d9\u00da\3\2\2\2\u00da\u00dc\3\2"+
		"\2\2\u00db\u00d9\3\2\2\2\u00dc\u00dd\7\f\2\2\u00dd\u00de\7\36\2\2\u00de"+
		"\u00df\7\r\2\2\u00df\u00fb\3\2\2\2\u00e0\u00e1\7\36\2\2\u00e1\u00e2\7"+
		"\n\2\2\u00e2\u00e7\7\"\2\2\u00e3\u00e4\7\13\2\2\u00e4\u00e6\7\"\2\2\u00e5"+
		"\u00e3\3\2\2\2\u00e6\u00e9\3\2\2\2\u00e7\u00e5\3\2\2\2\u00e7\u00e8\3\2"+
		"\2\2\u00e8\u00ea\3\2\2\2\u00e9\u00e7\3\2\2\2\u00ea\u00eb\7\f\2\2\u00eb"+
		"\u00ec\7\36\2\2\u00ec\u00fb\7\17\2\2\u00ed\u00ee\7\36\2\2\u00ee\u00ef"+
		"\7\n\2\2\u00ef\u00f4\7\"\2\2\u00f0\u00f1\7\13\2\2\u00f1\u00f3\7\"\2\2"+
		"\u00f2\u00f0\3\2\2\2\u00f3\u00f6\3\2\2\2\u00f4\u00f2\3\2\2\2\u00f4\u00f5"+
		"\3\2\2\2\u00f5\u00f7\3\2\2\2\u00f6\u00f4\3\2\2\2\u00f7\u00f8\7\f\2\2\u00f8"+
		"\u00f9\7\36\2\2\u00f9\u00fb\7\20\2\2\u00fa\u00c4\3\2\2\2\u00fa\u00d2\3"+
		"\2\2\2\u00fa\u00e0\3\2\2\2\u00fa\u00ed\3\2\2\2\u00fb\u00fc\3\2\2\2\u00fc"+
		"\u00fd\7\35\2\2\u00fd\u00ff\3\2\2\2\u00fe\u00a6\3\2\2\2\u00fe\u00be\3"+
		"\2\2\2\u00ff\27\3\2\2\2\u0100\u0101\7!\2\2\u0101\u0102\7\21\2\2\u0102"+
		"\u0103\7!\2\2\u0103\31\3\2\2\2\u0104\u0105\7!\2\2\u0105\u0106\7\22\2\2"+
		"\u0106\u0107\7!\2\2\u0107\33\3\2\2\2\u0108\u0109\7\23\2\2\u0109\u010a"+
		"\7!\2\2\u010a\35\3\2\2\2\u010b\u010e\7 \2\2\u010c\u010d\7\37\2\2\u010d"+
		"\u010f\7 \2\2\u010e\u010c\3\2\2\2\u010e\u010f\3\2\2\2\u010f\37\3\2\2\2"+
		"\u0110\u0111\t\2\2\2\u0111!\3\2\2\2\u0112\u0113\t\3\2\2\u0113#\3\2\2\2"+
		"\u0114\u0126\7 \2\2\u0115\u0116\7\34\2\2\u0116\u0117\7 \2\2\u0117\u0126"+
		"\7\35\2\2\u0118\u011a\t\4\2\2\u0119\u0118\3\2\2\2\u011a\u011d\3\2\2\2"+
		"\u011b\u0119\3\2\2\2\u011b\u011c\3\2\2\2\u011c\u011e\3\2\2\2\u011d\u011b"+
		"\3\2\2\2\u011e\u0122\7!\2\2\u011f\u0121\t\4\2\2\u0120\u011f\3\2\2\2\u0121"+
		"\u0124\3\2\2\2\u0122\u0120\3\2\2\2\u0122\u0123\3\2\2\2\u0123\u0126\3\2"+
		"\2\2\u0124\u0122\3\2\2\2\u0125\u0114\3\2\2\2\u0125\u0115\3\2\2\2\u0125"+
		"\u011b\3\2\2\2\u0126%\3\2\2\2\27\608@LT^`\u0094\u0096\u00a4\u00b6\u00cb"+
		"\u00d9\u00e7\u00f4\u00fa\u00fe\u010e\u011b\u0122\u0125";
	public static final ATN _ATN =
		new ATNDeserializer().deserialize(_serializedATN.toCharArray());
	static {
		_decisionToDFA = new DFA[_ATN.getNumberOfDecisions()];
		for (int i = 0; i < _ATN.getNumberOfDecisions(); i++) {
			_decisionToDFA[i] = new DFA(_ATN.getDecisionState(i), i);
		}
	}
}