import com.petebevin.markdown.MarkdownProcessor;


public class Test {
	
	public static void main(String[] args) throws Exception {
		
		MarkdownProcessor processor = new MarkdownProcessor();
		
		System.out.println(processor.markdown("# ������\n > �����"));
	  }
}
