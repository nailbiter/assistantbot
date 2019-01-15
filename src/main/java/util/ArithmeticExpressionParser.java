package util;

import java.util.HashSet;
import java.util.regex.Pattern;

import org.apache.commons.collections4.Transformer;

public class ArithmeticExpressionParser {
	
//	private Transformer<String, Double> parseDouble_;
	private HashSet<String> decimalalSeparators_;
	private int maximalNumberOfDecimalSigns_;
	
	public ArithmeticExpressionParser() {
		maximalNumberOfDecimalSigns_ = -1;
		
		decimalalSeparators_ = new HashSet<String>();
			decimalalSeparators_.add(".");
			decimalalSeparators_.add(",");
	}
	public ArithmeticExpressionParser(int maximalNumberOfDecimalSigns) {
		this();
		maximalNumberOfDecimalSigns_ = maximalNumberOfDecimalSigns;
	}
	public double simpleEvalDouble(String expr) throws AssistantBotException {
		if( !Pattern.matches(String.format("[0-9-+*%s]+", Util.CharSetToRegex(decimalalSeparators_)), expr)) {
			throw new AssistantBotException(AssistantBotException.Type.ARITHMETICEXPRESSIONPARSER,
					String.format("cannot eval \"%s\"", expr));
		}
		String[] split = expr.split("\\+");
		double res = 0.0;
		for(String part:split) {
			System.err.format("part=%s\n", part);
			double res1 = 1.0;
			String[] split1 = part.split("\\*");
			for(String part1:split1) {
				double d = parseDouble(part1);
				System.err.format("part1=\"%s\", d=%f\n", part,d);
				res1 *= d;
			}
			res += res1;
		}
			
		if(maximalNumberOfDecimalSigns_>=0)
			res = Truncate(res,maximalNumberOfDecimalSigns_);
		return res;
	}
	private static double Truncate(double res, int num) {
		int pow = (int) Math.pow(10, num);
		return Math.floor(res*pow)/pow;
	}
	private double parseDouble(String what) throws AssistantBotException {
		if( maximalNumberOfDecimalSigns_ < 0 ) {
			return Double.parseDouble(what);
		} else {
			for(String sep:decimalalSeparators_) {
				int pos = what.indexOf(sep);
				if( pos < 0 )
					continue;
				String part1 = what.substring(0, pos)
						,part2 = what.substring(pos + sep.length());
				int num1 = Integer.parseInt(part1)
						,num2 = part2.isEmpty()?0:Integer.parseInt(part2)
								,digitNum = DigitNum(num2)
								;
				if( digitNum > maximalNumberOfDecimalSigns_ )
					throw new AssistantBotException(AssistantBotException.Type.ARITHMETICEXPRESSIONPARSER
							,String.format("too many digits after sep in %s", what));
				return num1 + num2/Math.pow(10.0, digitNum);
			}
			return Integer.parseInt(what);
//			throw new AssistantBotException(AssistantBotException.Type.ARITHMETICEXPRESSIONPARSER,
//					String.format("cannot parse double \"%s\"", what));
		}
	}
	private static int DigitNum(int num) throws AssistantBotException {
		int res = 0;
		if(num<0)
			throw new AssistantBotException(AssistantBotException.Type.ARITHMETICEXPRESSIONPARSER,
					String.format("cannot DigitNum on negative %d", num));
		for(int power = 1; num >= power; power*=10,res++);
		return res;
	}
	public static double SimpleEvalDouble(String expr) throws AssistantBotException {
		return new ArithmeticExpressionParser().simpleEvalDouble(expr);
	}
	public static int SimpleEvalInt(String expr) throws AssistantBotException {
		if( !Pattern.matches("[0-9-+*]+", expr)) {
			throw new AssistantBotException(AssistantBotException.Type.ARITHMETICEXPRESSIONPARSER,
					String.format("cannot eval \"%s\"", expr));
		}
		String[] split = expr.split("\\+");
		int res = 0;
		for(String part:split) {
			int res1 = 1;
			String[] split1 = part.split("\\*");
			for(String part1:split1) {
				res1 *= Integer.parseInt(part1);
			}
			res += res1;
		}
			
		return res;
	}
}
