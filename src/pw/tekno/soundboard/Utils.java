package pw.tekno.soundboard;

import com.github.axet.vget.VGet;
import com.github.axet.vget.info.VGetParser;
import com.github.axet.vget.info.VideoInfo;
import com.github.axet.vget.vhs.YouTubeMPGParser;
import com.skype.Call;
import com.skype.Skype;
import it.sauronsoftware.jave.AudioAttributes;
import it.sauronsoftware.jave.Encoder;
import it.sauronsoftware.jave.EncoderException;
import it.sauronsoftware.jave.EncodingAttributes;
import org.apache.commons.lang3.StringUtils;

import javax.sound.sampled.*;
import java.io.File;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.URL;
import java.util.concurrent.atomic.AtomicBoolean;

public class Utils
{
	private static boolean isPlaying = false;
	private static SourceDataLine soundLine;
	private static TargetDataLine inputLine;
	private static ServerSocket socket;
	private static AudioInputStream audioInputStream = null;

	public static void downloadVideo(String videoURL, String targetName) throws Exception
	{
		File path = new File("files");
		try
		{
			URL web = new URL(videoURL);

			VGetParser user = new YouTubeMPGParser();
			VideoInfo info = user.info(web);

			VGet v = new VGet(info, path);
			v.extract(user, new AtomicBoolean(false), () -> {});

			String titleName = maxFileNameLength(replaceBadChars(info.getTitle()));
			String ext = info.getInfo().getContentType().replaceFirst("video/", "").replaceAll("x-", "");
			File f;
			Integer duplicateCount = 0;
			do {
				String add = duplicateCount > 0 ? " (".concat(duplicateCount.toString()).concat(")") : "";

				f = new File(path.getPath(), titleName + add + "." + ext);
				duplicateCount += 1;
			} while (f.exists());

			File targetFile;
			duplicateCount = 0;
			do {
				String add = duplicateCount > 0 ? " (".concat(duplicateCount.toString()).concat(")") : "";

				targetFile = new File(path.getPath(), targetName + add + ".wav");
				duplicateCount += 1;
			} while (targetFile.exists());

			v.download(user, new AtomicBoolean(false), () -> {});
			convertToWAV(f, targetFile);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static void convertToWAV(File videoFile, File targetFile) throws IOException, UnsupportedAudioFileException, EncoderException
	{
		AudioAttributes audio = new AudioAttributes();
		audio.setCodec("pcm_s16le");
		audio.setBitRate(16);
		audio.setChannels(1);
		audio.setSamplingRate(16000);

		EncodingAttributes attrs = new EncodingAttributes();
		attrs.setFormat("wav");
		attrs.setAudioAttributes(audio);
		Encoder encoder = new Encoder();

		try {
			encoder.encode(videoFile, targetFile, attrs);
		} catch (IllegalArgumentException | EncoderException e) {
			e.printStackTrace();
		}
		videoFile.delete();
	}

	public static void open() throws Exception
	{
		AudioFormat audioFormat = new AudioFormat(16000.0f, 16, 1, true, false);

		DataLine.Info info = new DataLine.Info(SourceDataLine.class, audioFormat);
		soundLine = (SourceDataLine) AudioSystem.getLine(info);
		soundLine.open(audioFormat);
		soundLine.start();


		DataLine.Info info2 = new DataLine.Info(TargetDataLine.class, audioFormat);
		inputLine = (TargetDataLine) AudioSystem.getLine(info2);
		inputLine.open(audioFormat);
		inputLine.start();
	}

	public static void stop()
	{
		isPlaying = false;
	}

	public static void playSound(String name)
	{
		isPlaying = false;

		try
		{
			if(socket != null)
			{
				socket.close();
				socket = null;
			}

			new UtilsThread(name).start();
		}
		catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static void playSoundCurrentCall(final String name) throws Exception
	{
		Call[] calls = Skype.getAllActiveCalls();
		if(calls.length < 1) return;

		audioInputStream = AudioSystem.getAudioInputStream(new File(name));
		if(audioInputStream.getFormat().getSampleRate() != 16000.0f || audioInputStream.getFormat().getSampleSizeInBits() != 16)
		{
			System.out.println("fuck.");
			return;
		}

		socket = new ServerSocket(8219);
		for(Call c : calls)
		{
			c.setPortInput(8219);
		}

		Socket sock = socket.accept();

		final byte[] arr = new byte[128];
		final byte[] arr2 = new byte[128];
		final byte[] out = new byte[128];

		isPlaying = true;
		while(isPlaying)
		{
			int soundLength = audioInputStream.read(arr, 0, arr.length);
			inputLine.read(arr2, 0, arr2.length);
			for (int i = 0; i < soundLength; i += 2)
			{
				short audioSample = (short)((short)((arr[i + 1] & 0xFF) << 8) | (arr[i] & 0xFF));
				short audioSample2 = (short)((short)((arr2[i + 1] & 0xFF) << 8) | (arr2[i] & 0xFF));

				audioSample /= 8;

				out[i] = (byte)audioSample;
				out[i + 1] = (byte)(audioSample >> 8);

				audioSample += audioSample2;

				arr[i] = (byte)audioSample;
				arr[i + 1] = (byte)(audioSample >> 8);
			}

			if(soundLength > 0)
			{
				soundLine.write(out, 0, soundLength);
				sock.getOutputStream().write(arr, 0, soundLength);
				sock.getOutputStream().flush();
			}
			else
			{
				isPlaying = false;
			}
		}

		for(Call c : calls)
		{
			c.clearPortInput();
		}

		sock.close();

		audioInputStream.close();
		audioInputStream = null;

		socket.close();
		socket = null;
	}

	public static String maxFileNameLength(String str)
	{
		int max = 255;
		if(str.length() > max)
			str = str.substring(0, max);
		return str;
	}

	public static String replaceBadChars(String f)
	{
		String replace = " ";
		f = f.replaceAll("/", replace);
		f = f.replaceAll("\\\\", replace);
		f = f.replaceAll(":", replace);
		f = f.replaceAll("\\?", replace);
		f = f.replaceAll("\"", replace);
		f = f.replaceAll("\\*", replace);
		f = f.replaceAll("<", replace);
		f = f.replaceAll(">", replace);
		f = f.replaceAll("\\|", replace);
		f = f.trim();
		f = StringUtils.removeEnd(f, ".");
		f = f.trim();

		String ff;
		while(!(ff = f.replaceAll("  ", " ")).equals(f))
		{
			f = ff;
		}

		return f;
	}



	static class UtilsThread extends Thread
	{
		private String songName;

		UtilsThread(String s)
		{
			this.songName = s;
		}

		public void run()
		{
			try
			{
				Utils.playSoundCurrentCall(this.songName);
			}
			catch(Exception a)
			{
				a.printStackTrace();
				System.out.println("socket closed");
			}
			finally
			{
				if(audioInputStream != null)
				{
					try
					{
						audioInputStream.close();
						audioInputStream = null;
					} catch(IOException e)
					{
						e.printStackTrace();
					}
				}
			}
		}
	}
}