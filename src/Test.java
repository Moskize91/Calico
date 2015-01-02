import javax.script.Bindings;
import javax.script.ScriptContext;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;

public class Test {
	
	public static void main(String[] args) throws Exception {
	    ScriptEngineManager manager = new ScriptEngineManager();
	    ScriptEngine engine = manager.getEngineByName("nashorn");
	    engine.put("a", 1);
	    engine.put("b", 5);
	    engine.put("c", 3);
	    Bindings bindings = engine.getBindings(ScriptContext.ENGINE_SCOPE);
	    Object a = bindings.get("a");
	    Object b = bindings.get("b");
	    System.out.println("a = " + a);
	    System.out.println("b = " + b);
	    
	    System.out.println(engine.eval("c"));
	    
	    System.out.println(bindings.keySet());
	  }
}
