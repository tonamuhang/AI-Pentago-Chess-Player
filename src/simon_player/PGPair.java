package simon_player;

// Pair class, implementation from https://www.techiedelight.com/implement-pair-class-java/
class PGPair<U, V>
{
	public final U first;   	// first field of a Pair
	public final V second;  	// second field of a Pair

	// Constructs a new Pair with specified values
	private PGPair(U first, V second)
	{
		this.first = first;
		this.second = second;
	}

	public U getKey() {
		return first;
	}
	
	public V getValue() {
		return second;
	}
	@Override
	// Checks specified object is "equal to" current object or not
	public boolean equals(Object o)
	{
		if (this == o)
			return true;

		if (o == null || getClass() != o.getClass())
			return false;

		PGPair<?, ?> pair = (PGPair<?, ?>) o;

		// call equals() method of the underlying objects
		if (!first.equals(pair.first))
			return false;
		return second.equals(pair.second);
	}

	@Override
	// Computes hash code for an object to support hash tables
	public int hashCode()
	{
		// use hash codes of the underlying objects
		return 31 * first.hashCode() + second.hashCode();
	}

	@Override
	public String toString()
	{
		return "(" + first + ", " + second + ")";
	}

	// Factory method for creating a Typed Pair immutable instance
	public static <U, V> PGPair <U, V> of(U a, V b)
	{
		// calls private constructor
		return new PGPair<>(a, b);
	}
}
