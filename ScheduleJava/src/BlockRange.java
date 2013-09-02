
public class BlockRange {
	
	public static BlockRange zero = new BlockRange(0, 0);
	
	public int start;
	public int end;
	
	public BlockRange(int start, int end) {
		this.start = start;
		this.end = end;
	}
}
