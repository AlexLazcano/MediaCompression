import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.filechooser.FileNameExtensionFilter;

import java.awt.*;

import java.awt.event.*;
import java.io.File;

public class GraphicJFrame extends JFrame {
	// sound
	private static SoundPanel audioPanel = new SoundPanel();
	private static JLabel fileLabel = new JLabel("No File Selected");
	private static JButton selectFileButton = new JButton("Select file");
	private static JLabel samplingRateLabel = new JLabel("Sampling Rate: ");
	private static JLabel totalSamplesLabel = new JLabel("Total Samples: ");
	private static JLabel legend = new JLabel("Left - Green | Right - Red | Mono - Black");
	private static JLabel CompressionRatioSound = new JLabel("Compression Ratio: ");
	// image
	private static ImagePanel imagePanel = new ImagePanel();
	private static JLabel imageFileLabel = new JLabel("No File Selected");
	private static JButton selectImageFileButton = new JButton("Select file");
	// private static JButton ditherButton = new JButton("Next");

	private static JButton ImageButton = new JButton("Image");
	private static JButton SoundButton = new JButton("Sound");
	private static JButton ExitButton = new JButton("Exit");

	private static Boolean soundActivated = false;
	private static Boolean imageActivated = false;

	public GraphicJFrame() {

		setSize(500, 500);
		setTitle("Project 1 - CMPT 365");
		setVisible(true);
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

		// setLayout(new FlowLayout());
		setLayout(new BoxLayout(getContentPane(), BoxLayout.Y_AXIS));

		getRootPane().setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
		getContentPane().add(ImageButton);
		getContentPane().add(SoundButton);

		// pack();

		ImageButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if (soundActivated) {
					removeSound();
				}
				imageActivated = true;
				soundActivated = false;
				addImage();

				revalidate();
				repaint();
				pack();
			}
		});
		SoundButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if (imageActivated) {
					removeImage();
				}
				soundActivated = true;
				imageActivated = false;
				addSound();

				revalidate();
				repaint();
				pack();
			}
		});
		selectFileButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				soundFileButtonClicked(e);
			}
		});
		ExitButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				System.exit(0);
			}
		});

		selectImageFileButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				imageFileButtonClicked(e);
			}
		});

		// ditherButton.addActionListener(new ActionListener() {
		// 	public void actionPerformed(ActionEvent e) {
		// 		ditherButtonClicked(e);
		// 	}
		// });
	}

	public void addSound() {
		getContentPane().add(fileLabel);
		getContentPane().add(selectFileButton);
		getContentPane().add(samplingRateLabel);
		getContentPane().add(totalSamplesLabel);
		getContentPane().add(legend);
		getContentPane().add(CompressionRatioSound);
		getContentPane().add(audioPanel);
		getContentPane().add(ExitButton);
	}

	public void removeSound() {

		getContentPane().remove(fileLabel);
		getContentPane().remove(selectFileButton);
		getContentPane().remove(samplingRateLabel);
		getContentPane().remove(totalSamplesLabel);
		getContentPane().remove(legend);
		getContentPane().remove(CompressionRatioSound);
		getContentPane().remove(audioPanel);
	}

	public void addImage() {

		getContentPane().add(imageFileLabel);
		getContentPane().add(selectImageFileButton);
		getContentPane().add(imagePanel);
		// getContentPane().add(ditherButton);
		// ditherButton.setEnabled(false);
		getContentPane().add(ExitButton);
	}

	public void removeImage() {
		getContentPane().remove(imageFileLabel);
		getContentPane().remove(selectImageFileButton);
		getContentPane().remove(imagePanel);
		// getContentPane().remove(ditherButton);
		// ditherButton.setEnabled(false);
	}

	// public static void ditherButtonClicked(ActionEvent e) {
	// 	imagePanel.toggleDither();
	// }

	public static void readWav(File f) {
		try {

			AudioInputStream a = AudioSystem.getAudioInputStream(f);
			// sampling rate
			int samplingRate = (int) a.getFormat().getSampleRate();
			samplingRateLabel.setText("Sampling Rate : " + samplingRate + "hz");

			// number of bytes per frame
			int frameSize = a.getFormat().getFrameSize();
			System.out.println("frameSize: " + frameSize);

			// number of frames
			int numFrames = (int) (a.getFrameLength());
			System.out.println("numFrames: " + numFrames);

			// number of bytes
			int numBytes = (int) (numFrames * frameSize);
			System.out.println("numBytes: " + numBytes);

			// get size of bits per sample
			int bitsPerSample = a.getFormat().getSampleSizeInBits();
			System.out.println("bitsPerSample: " + bitsPerSample);

			// number of channels
			int numChannels = a.getFormat().getChannels();
			System.out.println("numChannels: " + numChannels);
			System.out.println("---------------------------------");

			// get total number of samples
			int totalSamples = (int) (numBytes / (numChannels * bitsPerSample / 8));
			totalSamplesLabel.setText("Total Samples: " + totalSamples);

			byte[] audioBytes = new byte[numBytes];

			a.read(audioBytes);

			audioPanel.repaint(audioBytes, numFrames, bitsPerSample, numChannels);

		} catch (Exception e) {
			e.printStackTrace();
			System.out.println(e);

		}
	}

	public static void readPNG(File f) {
		try {
			imagePanel.repaint(f);
		} catch (Exception e) {
			e.printStackTrace();
			System.out.println(e);
		}
	}

	public static void main(String[] args) {
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					GraphicJFrame frame = new GraphicJFrame();
					frame.setVisible(true);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});

	}

	public static void soundFileButtonClicked(ActionEvent e) {
		fileLabel.setText("File Selected: ");
		JFileChooser fileChooser = new JFileChooser();
		FileNameExtensionFilter filter = new FileNameExtensionFilter("WAV files", "wav");
		fileChooser.setFileFilter(filter);

		int res = fileChooser.showOpenDialog(null);
		if (res == JFileChooser.APPROVE_OPTION) {
			File file = fileChooser.getSelectedFile();
			fileLabel.setText("File Selected: " + fileChooser.getSelectedFile().getName());
			readWav(file);
			
			if (SoundPanel.CompressionRatio != 0) {
				String ratio = String.format("Compression Ratio: %.2f", SoundPanel.CompressionRatio);
				CompressionRatioSound.setText(ratio);
			}
		}

	}

	public void imageFileButtonClicked(ActionEvent e) {
		imageFileLabel.setText("File Selected: ");
		JFileChooser fileChooser = new JFileChooser();
		FileNameExtensionFilter filter = new FileNameExtensionFilter("PNG files", "png");

		fileChooser.setFileFilter(filter);

		int res = fileChooser.showOpenDialog(null);
		if (res == JFileChooser.APPROVE_OPTION) {
			File file = fileChooser.getSelectedFile();
			imageFileLabel.setText("File Selected: " + fileChooser.getSelectedFile().getName());
			// ditherButton.setEnabled(true);
			readPNG(file);
			pack();
		}

	}

}
