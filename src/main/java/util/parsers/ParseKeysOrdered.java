package util.parsers;

import java.util.ArrayList;

import org.apache.commons.collections4.IterableUtils;
import org.apache.commons.collections4.Predicate;
import org.apache.commons.collections4.Transformer;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.ImmutableTriple;

import util.AssistantBotException;
import util.Message;
import util.parsers.ParseCommentLine.Mode;
import util.parsers.ParseOrdered.ArgTypes;

public class ParseKeysOrdered {
	private ArrayList<ImmutableTriple<String,Transformer<ImmutablePair<Object,ArrayList<Object>>,Object>,ArrayList<ArgTypes>>> dispatchTable_ = new ArrayList<ImmutableTriple<String,Transformer<ImmutablePair<Object,ArrayList<Object>>,Object>,ArrayList<ArgTypes>>>();
	private final String TAGPREFIX = "#";
	private Mode mode_;
	public ParseKeysOrdered(ParseCommentLine.Mode mode) {
		mode_ = mode;
	}
	public ParseKeysOrdered addHandler(String name, Transformer<ImmutablePair<Object,ArrayList<Object>>,Object> t, ArgTypes... types) {
		ArrayList<ArgTypes> typelist = new ArrayList<ArgTypes>(); 
		for(ArgTypes type:types) {
			typelist.add(type);
		}
		dispatchTable_.add(new ImmutableTriple<String,Transformer<ImmutablePair<Object,ArrayList<Object>>,Object>,ArrayList<ArgTypes>>(name, t, typelist));
		return this;
	}
	public Transformer<Object,Object> createPipeline(String message) throws AssistantBotException{
		ArrayList<ImmutablePair<Transformer<ImmutablePair<Object, ArrayList<Object>>, Object>, ArrayList<Object>>> processors = 
				new ArrayList<ImmutablePair<Transformer<ImmutablePair<Object,ArrayList<Object>>,Object>,ArrayList<Object>>>();
		while( message.startsWith(TAGPREFIX) ) {
			String[] split = message.split(ParseOrdered.SPLITPAT,2);
			String name = split[0].substring(TAGPREFIX.length());
			
			ImmutableTriple<String,Transformer<ImmutablePair<Object,ArrayList<Object>>,Object>,ArrayList<ArgTypes>> item = 
					IterableUtils.find(dispatchTable_
					, new Predicate<ImmutableTriple<String,Transformer<ImmutablePair<Object,ArrayList<Object>>,Object>,ArrayList<ArgTypes>>>(){
						@Override
						public boolean evaluate(
								ImmutableTriple<String, Transformer<ImmutablePair<Object, ArrayList<Object>>, Object>, ArrayList<ArgTypes>> object) {
							return name.equals(object.left);
						}
			});
			
			ArrayList<Object> args = new ArrayList<Object>();
			message = split[1];
			for(ArgTypes it:item.right) {
				Object arg = null;
				switch(it) {
				case integer:{
					String[] ssplit = message.split(ParseOrdered.SPLITPAT,2);
					arg = Integer.parseInt(ssplit[0]);
					message = ssplit[1];
					break;
				}	
				case remainder:
					arg = message;
					message = "";
					break;
				case string:{
					String[] ssplit = message.split(ParseOrdered.SPLITPAT,2);
					arg = ssplit[0];
					message = (ssplit.length>1)?ssplit[1]:"";
					break;
				}
				default:
					throw new AssistantBotException(
							AssistantBotException.Type.PARSEKEYSORDERED
							,String.format("unknown type \"%s\"", item.right.get(0)));
//					break;
				}
				if( arg != null ) {
					args.add(arg);
					System.err.format("binding argument %s of type %s for %s\n"
							,arg,it,name);
				}
			}
			
			if( args.size() == item.right.size() ) {
				switch(mode_) {
				case FROMLEFT:
					processors.add(new ImmutablePair<Transformer<ImmutablePair<Object,ArrayList<Object>>,Object>,ArrayList<Object>>(item.middle,args));
					break;
				case FROMRIGHT:
					processors.add(0,new ImmutablePair<Transformer<ImmutablePair<Object,ArrayList<Object>>,Object>,ArrayList<Object>>(item.middle,args));
					break;
				default:
					break;
				}
			} else {
				throw new AssistantBotException(AssistantBotException.Type.PARSEKEYSORDERED
						,String.format("%d != %d", args.size(),item.right.size())
						);
			}
		}
		
		return new Transformer<Object,Object>() {
			@Override
			public Object transform(Object arg0) {
				Object o = arg0;
				for(ImmutablePair<Transformer<ImmutablePair<Object, ArrayList<Object>>, Object>, ArrayList<Object>> processor:processors) {
					o = processor.left.transform(new ImmutablePair<Object,ArrayList<Object>>(o,processor.right));
				}
				return o;
			}
		};
	}
}
