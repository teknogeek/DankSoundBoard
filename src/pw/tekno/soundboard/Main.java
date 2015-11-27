package pw.tekno.soundboard;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

public class Main
{
	public static void main(String[] args) throws Exception
	{
		File filesFolder = new File("files");
		if(!filesFolder.exists()) filesFolder.mkdir();

		JFrame frame;
		frame = new JFrame("Sample Text");
		frame.setSize(600, 425);
		frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
		frame.setResizable(false);
		frame.setLocationRelativeTo(null);

		frame.setContentPane(new drawBackgroundImage(new ImageIcon(Main.class.getResource("/Images/Picture.png")).getImage()));

		//Youtube Link
		frame.setLayout(null);
		JTextArea linkInput = new JTextArea("");
		linkInput.setEditable(true);
		linkInput.setSize(178 , 22);
		linkInput.setLocation(362 , 302);

		//Name Input
		JTextArea nameInput = new JTextArea("");
		nameInput.setEditable(true);
		nameInput.setSize(178, 22);
		nameInput.setLocation(362 , 278);

		//ABORT MISSION
		JButton quit = new JButton(new ImageIcon(Main.class.getResource("/Images/Quit.png")));
		quit.setSize(280, 54);
		quit.setLocation(312, 329);
		quit.setPressedIcon(new ImageIcon(Main.class.getResource("/Images/QuitPush.png")));

		quit.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				System.exit(0);
			}
		});

		DefaultListModel<String> model = new DefaultListModel<>();
		File temp = new File("files\\sound.wav");
		File folder = new File(temp.getAbsolutePath().replace("\\" + temp.getName(), ""));
		System.out.println(folder.getAbsolutePath());
		File[] fileList = folder.listFiles();
		if(fileList != null)
		{
			for(File f : fileList)
			{
				if(f.getName().endsWith(".wav"))
				{
					model.addElement(f.getName().replace(".wav", ""));
				}
			}
		}

		final JList<String> songList = new JList<>();
		songList.setBorder(null);
		songList.setFont(new Font("Comic Sans MS", 0, 13));

		songList.setModel(model);
		songList.setOpaque(false);

		JScrollPane scrollPane = new JScrollPane(songList);
		scrollPane.setViewportBorder(null);
		scrollPane.setBorder(null);
		scrollPane.getViewport().setOpaque(false);
		scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
		scrollPane.setOpaque(false);
		scrollPane.setSize(282, 280);
		scrollPane.setLocation(21, 45);


		//PLAY BUTTON
		JButton play = new JButton(new ImageIcon(Main.class.getResource("/Images/Play.png")));
		play.setSize(54, 54);
		play.setLocation(21, 329);
		play.setPressedIcon(new ImageIcon(Main.class.getResource("/Images/PlayPush.png")));
		play.addActionListener(new ActionListener()
		{
			@Override
			public void actionPerformed(ActionEvent e)
			{
				String songName = "files\\" + songList.getSelectedValue() + ".wav";
				Utils.playSound(songName);
			}
		});

		//STOP SONG
		JButton stop = new JButton(new ImageIcon(Main.class.getResource("/Images/Stop.png")));
		stop.setSize(54 , 54);
		stop.setLocation(80, 329);
		stop.setPressedIcon(new ImageIcon(Main.class.getResource("/Images/StopPush.png")));
		stop.addActionListener(new ActionListener()
		{
			@Override
			public void actionPerformed(ActionEvent e)
			{
				Utils.stop();
			}
		});


		//REMOVE SONG
		JButton remove = new JButton(new ImageIcon(Main.class.getResource("/Images/Remove.png")));
		remove.setSize(54, 54);
		remove.setLocation(249 , 329);
		remove.setPressedIcon(new ImageIcon(Main.class.getResource("/Images/DeletePush.png")));
		remove.addActionListener(new ActionListener()
		{
			@Override
			public void actionPerformed(ActionEvent e)
			{
				Utils.stop();
				int index = songList.getSelectedIndex();
				String songName = model.get(index);
				try
				{
					System.gc();
					Files.delete(Paths.get("files\\" + songName + ".wav"));
					model.removeElementAt(index);
				}
				catch(IOException e1)
				{
					e1.printStackTrace();
				}
			}
		});

		//ADD SONG
		JButton add = new JButton(new ImageIcon(Main.class.getResource("/Images/Add.png")));
		add.setSize(48, 48);
		add.setLocation(544, 277);
		add.setPressedIcon(new ImageIcon(Main.class.getResource("/Images/AddPush.png")));
		add.addActionListener(new ActionListener()
		{
			@Override
			public void actionPerformed(ActionEvent e)
			{
				try
				{
					Utils.downloadVideo(linkInput.getText(), nameInput.getText());
					model.addElement(nameInput.getText());
				}
				catch(Exception e1)
				{
					e1.printStackTrace();
				}
			}
		});

		frame.add(nameInput);

		frame.add(linkInput);
		frame.add(scrollPane);
		frame.add(remove);
		frame.add(play);
		frame.add(stop);

		frame.add(add);
		frame.add(quit);
		frame.setVisible(true);

		Utils.open();
	}
}