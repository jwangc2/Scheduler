
public class BlockRange {
	
	public static BlockRange zero = new BlockRange(0, 0);
	
	public int start;
	public int end;
	public int pad;
	
	public BlockRange(int start, int end) {
		this(start, end, 0);
	}
	
	public BlockRange(int start, int end, int pad) {
		this.start = start;
		this.end = end;
		this.pad = pad;
	}
}
