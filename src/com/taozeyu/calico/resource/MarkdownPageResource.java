package com.taozeyu.calico.resource;

import java.io.File;
import java.io.IOException;
import java.io.Reader;
import java.util.HashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.petebevin.markdown.MarkdownProcessor;
import com.taozeyu.calico.util.EscapeHelper;

public class MarkdownPageResource extends AbstractPageResource {

	private static final MarkdownProcessor MarkdwonProcessor = new MarkdownProcessor();
	
	MarkdownPageResource(File resourceFile, String resourcePath) {
		super(resourceFile, resourcePath);
	}

	@Override
	public String createContent() throws IOException {
		try (Reader reader = createResourceFileReaderByJumpOverHead()) {
			String content = getStringFromReader(reader);
			CodeBlockContainer codeBlockContainer = new CodeBlockContainer();
			content = codeBlockContainer.pares(content);
			content = MarkdwonProcessor.markdown(content);
			content = codeBlockContainer.replaceHolderWithCodeBlock(content);
			return content;
		}
	}

	private static final String MagicNumberLeft = "IJSHAA";
	private static final String MagicNumberRight = "AAHSJI";

	private static class CodeBlockContainer {

		private final HashMap<Integer, CodeBlock> container = new HashMap<>();
		private int nextCodeBlockId = 0;

		private String pares(String content) {
			StringBuilder sb = new StringBuilder();
			CodeBlock currentCodeBlock = null;
			for (int i=0; i<content.length(); ++i) {
				char c = content.charAt(i);
				if (c == '`') {
					if ("```".equals(content.substring(i, i + "```".length()))) {
						if (currentCodeBlock != null) {
							sb.append(currentCodeBlock.codeBlockHolder());
							storeCodeBlock(currentCodeBlock);
							currentCodeBlock = null;
						} else {
							currentCodeBlock = generateCodeBlockWithPreTag(true);
						}
						i += "```".length() - 1;

					} else if ("``".equals(content.substring(i, i + "``".length()))) {

						if (currentCodeBlock != null) {
							currentCodeBlock.reciveChar('`');
						} else {
							sb.append("``");
						}
						i += "``".length() - 1;

					} else {
						if (currentCodeBlock != null) {
							sb.append(currentCodeBlock.codeBlockHolder());
							storeCodeBlock(currentCodeBlock);
							currentCodeBlock = null;
						} else {
							currentCodeBlock = generateCodeBlockWithPreTag(false);
						}
					}
				} else {
					if (currentCodeBlock != null) {
						currentCodeBlock.reciveChar(c);
					} else {
						sb.append(c);
					}
				}
			}
			return sb.toString();
		}

		private String replaceHolderWithCodeBlock(String content) {
			Pattern pattern = Pattern.compile(MagicNumberLeft + "\\d+" + MagicNumberRight);
			Matcher matcher = pattern.matcher(content);
			StringBuilder sb = new StringBuilder();
			int readStringLastIndex = 0;
			while (matcher.find()) {
				sb.append(content.substring(readStringLastIndex, matcher.start()));
				int id = getCodeBlockId(matcher.group());
				CodeBlock codeBlock = container.get(id);
				sb.append(codeBlock.getContent());
				readStringLastIndex = matcher.end();
			}
			sb.append(content.substring(readStringLastIndex));
			return sb.toString();
		}

		private int getCodeBlockId(String holder) {
			Matcher matcher = Pattern.compile("\\d+").matcher(holder);
			matcher.find();
			return Integer.valueOf(matcher.group());
		}

		private void storeCodeBlock(CodeBlock codeBlock) {
			container.put(codeBlock.id, codeBlock);
		}

		private CodeBlock generateCodeBlockWithPreTag(boolean preTag) {
			CodeBlock codeBlock = new CodeBlock();
			codeBlock.preTag = preTag;
			codeBlock.id = nextCodeBlockId++;
			return codeBlock;
		}
	}

	private static class CodeBlock {

		private int id;
		private boolean preTag;

		private final StringBuilder stringBuilder = new StringBuilder();

		private void reciveChar(char c) {
			stringBuilder.append(c);
		}

		private String codeBlockHolder() {
			return MagicNumberLeft + id + MagicNumberRight;
		}

		private String getContent() {
			String content = stringBuilder.toString();
			if (!preTag) {
				content = content.replaceAll("(\\s|\\n)+", " ").trim();
			}
			content = EscapeHelper.escape(content);
			String head = preTag? "<div class='highlight'><code><pre>": "<span class='highlight'><code>";
			String rear = preTag? "</pre></code></div>": "</span></code>";
			return head + content + rear;
		}
	}
}
