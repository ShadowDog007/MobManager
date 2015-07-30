package ninja.mcknight.bukkit.mobmanager.bounty.config;

public enum BountyType
{
	MONEY("%1$.2f", "%2$s"),
	ITEM("%1$d", "%2$s"),
	EXP("%1$d", "%2$s");
	
	public final String amount;
	public final String mob;
	
	private BountyType(String amount, String mob)
	{
		this.amount = amount;
		this.mob = mob;
	}
}
