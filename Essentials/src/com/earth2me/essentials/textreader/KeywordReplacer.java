package com.earth2me.essentials.textreader;

import com.earth2me.essentials.ExecuteTimer;
import net.ess3.api.IEssentials;
import com.earth2me.essentials.User;
import com.earth2me.essentials.utils.DateUtil;
import com.earth2me.essentials.utils.DescParseTickFormat;
import com.earth2me.essentials.utils.NumberUtil;
import java.lang.management.ManagementFactory;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

import static com.earth2me.essentials.I18n._;
import com.earth2me.essentials.PlayerList;
import static com.earth2me.essentials.PlayerList.getMergedList;
import java.util.logging.Level;


public class KeywordReplacer implements IText
{
	private final transient IText input;
	private final transient List<String> replaced;
	private final transient IEssentials ess;
	private final transient boolean extended;
	private transient ExecuteTimer execTimer;

	public KeywordReplacer(final IText input, final CommandSender sender, final IEssentials ess)
	{
		this.input = input;
		this.replaced = new ArrayList<String>(this.input.getLines().size());
		this.ess = ess;
		this.extended = true;
		replaceKeywords(sender);
	}

	public KeywordReplacer(final IText input, final CommandSender sender, final IEssentials ess, final boolean extended)
	{
		this.input = input;
		this.replaced = new ArrayList<String>(this.input.getLines().size());
		this.ess = ess;
		this.extended = extended;
		replaceKeywords(sender);
	}

	private void replaceKeywords(final CommandSender sender)
	{
		String displayName, ipAddress, balance, mails, world;
		String worlds, online, unique, onlineList, date, time;
		String worldTime12, worldTime24, worldDate, plugins;
		String userName, version, address, tps, uptime;
		String coords;
		execTimer = new ExecuteTimer();
		execTimer.start();
		if (sender instanceof Player)
		{
			final User user = ess.getUser(sender);
			user.setDisplayNick();
			displayName = user.getDisplayName();
			ipAddress = user.getAddress() == null || user.getAddress().getAddress() == null ? "" : user.getAddress().getAddress().toString();
			address = user.getAddress() == null ? "" : user.getAddress().toString();
			execTimer.mark("User Grab");
			balance = NumberUtil.displayCurrency(user.getMoney(), ess);
			execTimer.mark("Economy");
			mails = Integer.toString(user.getMails().size());
			final Location location = user.getLocation();
			world = location == null || location.getWorld() == null ? "" : location.getWorld().getName();
			worldTime12 = DescParseTickFormat.format12(user.getWorld() == null ? 0 : user.getWorld().getTime());
			worldTime24 = DescParseTickFormat.format24(user.getWorld() == null ? 0 : user.getWorld().getTime());
			worldDate = DateFormat.getDateInstance(DateFormat.MEDIUM, ess.getI18n().getCurrentLocale()).format(DescParseTickFormat.ticksToDate(user.getWorld() == null ? 0 : user.getWorld().getFullTime()));
			coords = _("coordsKeyword", location.getBlockX(), location.getBlockY(), location.getBlockZ());
		}
		else
		{
			displayName = address = ipAddress = balance = mails = world = worldTime12 = worldTime24 = worldDate = coords = "";
		}
		execTimer.mark("Player variables");
		Map<String, List<User>> playerList = PlayerList.getPlayerLists(ess, extended);

		userName = sender.getName();
		int playerHidden = 0;
		for (Player p : ess.getServer().getOnlinePlayers())
		{
			if (ess.getUser(p).isHidden())
			{
				playerHidden++;
			}
		}
		online = Integer.toString(ess.getServer().getOnlinePlayers().length - playerHidden);
		unique = Integer.toString(ess.getUserMap().getUniqueUsers());
		execTimer.mark("Player list");

		final StringBuilder worldsBuilder = new StringBuilder();
		for (World w : ess.getServer().getWorlds())
		{
			if (worldsBuilder.length() > 0)
			{
				worldsBuilder.append(", ");
			}
			worldsBuilder.append(w.getName());
		}
		worlds = worldsBuilder.toString();

		final StringBuilder playerlistBuilder = new StringBuilder();
		for (Player p : ess.getServer().getOnlinePlayers())
		{
			if (ess.getUser(p).isHidden())
			{
				continue;
			}
			if (playerlistBuilder.length() > 0)
			{
				playerlistBuilder.append(", ");
			}
			playerlistBuilder.append(p.getDisplayName());
		}
		onlineList = playerlistBuilder.toString();

		final StringBuilder pluginlistBuilder = new StringBuilder();
		for (Plugin p : ess.getServer().getPluginManager().getPlugins())
		{
			if (pluginlistBuilder.length() > 0)
			{
				pluginlistBuilder.append(", ");
			}
			pluginlistBuilder.append(p.getDescription().getName());
		}
		plugins = pluginlistBuilder.toString();
		
		execTimer.mark("List builders");

		date = DateFormat.getDateInstance(DateFormat.MEDIUM, ess.getI18n().getCurrentLocale()).format(new Date());
		time = DateFormat.getTimeInstance(DateFormat.MEDIUM, ess.getI18n().getCurrentLocale()).format(new Date());

		version = ess.getServer().getVersion();

		tps = Double.toString(ess.getTimer().getAverageTPS());
		uptime = DateUtil.formatDateDiff(ManagementFactory.getRuntimeMXBean().getStartTime());

		execTimer.mark("Server Dates");

		for (int i = 0; i < input.getLines().size(); i++)
		{
			String line = input.getLines().get(i);

			line = line.replace("{PLAYER}", displayName);
			line = line.replace("{DISPLAYNAME}", displayName);
			line = line.replace("{USERNAME}", userName);
			line = line.replace("{BALANCE}", balance);
			line = line.replace("{MAILS}", mails);
			line = line.replace("{WORLD}", world);
			line = line.replace("{ONLINE}", online);
			line = line.replace("{UNIQUE}", unique);
			line = line.replace("{WORLDS}", worlds);
			line = line.replace("{PLAYERLIST}", onlineList);
			line = line.replace("{TIME}", time);
			line = line.replace("{DATE}", date);
			line = line.replace("{WORLDTIME12}", worldTime12);
			line = line.replace("{WORLDTIME24}", worldTime24);
			line = line.replace("{WORLDDATE}", worldDate);
			line = line.replace("{COORDS}", coords);
			line = line.replace("{TPS}", tps);
			line = line.replace("{UPTIME}", uptime);

			if (extended)
			{
				line = line.replace("{IP}", ipAddress);
				line = line.replace("{ADDRESS}", address);
				line = line.replace("{PLUGINS}", plugins);
				line = line.replace("{VERSION}", version);

				for (String groupName : playerList.keySet())
				{
					final List<User> groupUsers = playerList.get(groupName);
					if (groupUsers != null && !groupUsers.isEmpty())
					{
						line = line.replaceAll("\\{PLAYERLIST\\:" + groupName.toUpperCase() + "(?:\\:([^\\{\\}]*))?\\}",
											   PlayerList.listUsers(ess, groupUsers, " "));
					}
				}

				boolean doReplace = true;
				while (doReplace)
				{
					final String newLine = line.replaceAll("\\{PLAYERLIST\\:\\w*(?:\\:([^\\{\\}]*))?\\}", "$1");
					if (newLine.equals(line))
					{
						doReplace = false;
					}
					line = newLine;
				}
			}

			replaced.add(line);
		}
		execTimer.mark("String replace");
		final String timeroutput = execTimer.end();
		if (ess.getSettings().isDebug())
		{
			ess.getLogger().log(Level.INFO, "Keyword Replacer " + timeroutput);
		}

	}

	@Override
	public List<String> getLines()
	{
		return replaced;
	}

	@Override
	public List<String> getChapters()
	{
		return input.getChapters();
	}

	@Override
	public Map<String, Integer> getBookmarks()
	{
		return input.getBookmarks();
	}
}
