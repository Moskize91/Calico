import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;


public class Test {
	
	public static void main(String[] args) throws Throwable{
        ScriptEngineManager engineManager = new ScriptEngineManager();
        ScriptEngine engine = engineManager.getEngineByName("nashorn");
        engine.eval("function sum(a,b){return a+b;}");
        System.out.println(engine.eval("sum(1,2);"));
    }
}
