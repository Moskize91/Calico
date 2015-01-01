import javax.script.Bindings;
import javax.script.Invocable;
import javax.script.ScriptContext;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;


public class Test {
	
	public static void main(String[] args) throws Exception {
	    ScriptEngineManager manager = new ScriptEngineManager();
	    ScriptEngine engine = manager.getEngineByName("nashorn");
	    engine.put("a", 1);
	    engine.put("b", 5);
	    
	    Invocable invoker = new Invocable() {
			
			@Override
			public Object invokeMethod(Object thiz, String name, Object... args)
					throws ScriptException, NoSuchMethodException {
				return "invokeMethod";
			}
			
			@Override
			public Object invokeFunction(String name, Object... args)
					throws ScriptException, NoSuchMethodException {
				return "invokeMethod";
			}
			
			@Override
			public <T> T getInterface(Object thiz, Class<T> clasz) {
				return null;
			}
			
			@Override
			public <T> T getInterface(Class<T> clasz) {
				return null;
			}
		}; 

		engine.put("haha", invoker);
		
	    Bindings bindings = engine.getBindings(ScriptContext.ENGINE_SCOPE);
	    Object a = bindings.get("a");
	    Object b = bindings.get("b");
	    System.out.println("a = " + a);
	    System.out.println("b = " + b);
	    
	    System.out.println(engine.eval("haha();"));
	    
	    System.out.println(bindings.keySet());
	  }
}
