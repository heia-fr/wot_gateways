package ch.eiafr.knx.utils;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Enumeration;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.stream.StreamSource;

import org.jdom2.Document;
import org.jdom2.input.SAXBuilder;
import org.jdom2.output.Format;
import org.jdom2.output.XMLOutputter;
import org.jdom2.transform.JDOMResult;
import org.jdom2.transform.JDOMSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class XMLGenerator {

	private static final Logger logger = LoggerFactory
			.getLogger(XMLGenerator.class);

	/**
	 * Generate the xml file containing datapoints descriptions
	 * 
	 * @param p_ETSProjectFile
	 *            The path to the knxproj file
	 * @param p_TransformFile
	 *            The path to the xsl file
	 * @param p_OutputFilePath
	 *            The path where the xml file has to be written
	 * @throws Exception
	 */
	public static void generateXMLDatapoints(String p_ETSProjectFile,
			String p_TransformFile, String p_OutputFilePath) throws Exception {

		generateXMLDatapoints(p_ETSProjectFile, new FileInputStream(new File(p_TransformFile)), p_OutputFilePath);
	}
	
	/**
	 * Generate the xml file containing datapoints descriptions
	 * 
	 * @param p_ETSProjectFile
	 *            The path to the knxproj file
	 * @param p_TransformFileStream
	 *            The stream to the xsl file
	 * @param p_OutputFilePath
	 *            The path where the xml file has to be written
	 * @throws Exception
	 */
	public static void generateXMLDatapoints(String p_ETSProjectFile,
			InputStream p_TransformFile, String p_OutputFilePath) throws Exception {

		long startZip = System.currentTimeMillis();
		String l_zipPath = unzipETSProject(p_ETSProjectFile);
		long stopZip = System.currentTimeMillis();

		logger.debug("ZIP Time: " + (stopZip - startZip));

		long startXml = System.currentTimeMillis();
		generateXML(l_zipPath, p_TransformFile, p_OutputFilePath);
		long stopXml = System.currentTimeMillis();

		logger.debug("XML Time: " + (stopXml - startXml));
	}

	/**
	 * Unzip the ETS project file
	 * 
	 * @param p_ETSProjectFile
	 *            The path to the knxproj file
	 * @return The path to the unzipped directory
	 * @throws Exception
	 */
	private static String unzipETSProject(String p_ETSProjectFile)
			throws Exception {
		if (p_ETSProjectFile == null || p_ETSProjectFile.equals("")) {
			throw new Exception("The ETS project file is not specified");
		}

		String l_zipPath = null;
		/*
		 * STEP 1 : Create directory with the name of the zip file
		 * 
		 * For e.g. if we are going to extract c:/demo.zip create c:/demo
		 * directory where we can extract all the zip entries
		 */
		File l_sourceZip = new File(p_ETSProjectFile);
		l_zipPath = p_ETSProjectFile
				.substring(0, p_ETSProjectFile.length() - 8);
		File l_temp = new File(l_zipPath);
		l_temp.mkdir();

		/*
		 * STEP 2 : Extract entries while creating required sub-directories
		 */
		ZipFile l_zipFile = new ZipFile(l_sourceZip);
		Enumeration<? extends ZipEntry> l_entries = l_zipFile.entries();
		while (l_entries.hasMoreElements()) {
			ZipEntry l_entry = (ZipEntry) l_entries.nextElement();
			File l_destinationFilePath = new File(l_zipPath, l_entry.getName());

			// create directories if required.
			l_destinationFilePath.getParentFile().mkdirs();

			// if the entry is directory, leave it. Otherwise extract it.
			if (l_entry.isDirectory()) {
				continue;
			} else {
				/*
				 * Get the InputStream for current entry of the zip file using
				 * 
				 * InputStream getInputStream(Entry entry) method.
				 */
				BufferedInputStream l_bis = new BufferedInputStream(
						l_zipFile.getInputStream(l_entry));
				int b;
				byte l_buffer[] = new byte[1024];

				/*
				 * read the current entry from the zip file, extract it and
				 * write the extracted file.
				 */
				FileOutputStream l_fos = new FileOutputStream(
						l_destinationFilePath);
				BufferedOutputStream l_bos = new BufferedOutputStream(l_fos,
						1024);

				while ((b = l_bis.read(l_buffer, 0, 1024)) != -1) {
					l_bos.write(l_buffer, 0, b);
				}

				// flush the output stream and close it.
				l_bos.flush();
				l_bos.close();

				// close the input stream.
				l_bis.close();
			}
		}
		return l_zipPath;
	}

	/**
	 * Do the xsl transformation
	 * 
	 * @param p_KNXProjectPath
	 *            The path to the unzipped project
	 * @param p_TransformFile
	 *            The stream to the xsl file
	 * @param p_OutputFilePath
	 *            The path where the xml file has to be written
	 * @throws Exception
	 */
	private static void generateXML(String p_KNXProjectPath,
			InputStream p_TransformFile, String p_OutputFilePath) throws Exception {
		// Find the P-**** directory
		File l_path = new File(p_KNXProjectPath);
		File[] l_dirs = l_path.listFiles(new FileFilter() {

			@Override
			public boolean accept(File pathname) {
				if (pathname.isDirectory()
						&& pathname.getName().startsWith("P-"))
					return true;
				return false;
			}
		});

		if (l_dirs.length == 0)
			throw new Exception("Config file 0.xml not found");

		JDOMResult l_documentResult = new JDOMResult();
		Document l_document = null;
		long first = System.currentTimeMillis();
		// Copy the xsl file into the KNX project directory
		copyfile(p_TransformFile, p_KNXProjectPath + "/KNXTransformer.xsl");

		// Load the xsl file to create a transformer
		TransformerFactory l_factory = TransformerFactory.newInstance();
		Transformer l_transformer = l_factory.newTransformer(new StreamSource(
				p_KNXProjectPath + "/KNXTransformer.xsl"));
		long second = System.currentTimeMillis();

		logger.debug("Load xsl " + (second - first));

		// Load the source xml document
		SAXBuilder l_sax = new SAXBuilder();
		Document l_input = l_sax.build(new File(l_dirs[0].getAbsolutePath()
				+ "/0.xml"));
		long third = System.currentTimeMillis();

		logger.debug("Load xml " + (third - second));

		l_transformer.transform(new JDOMSource(l_input), l_documentResult);
		long fourth = System.currentTimeMillis();

		logger.debug("Transform xsl " + (fourth - third));

		// Write the result into the destination file
		l_document = l_documentResult.getDocument();
		XMLOutputter l_outputter = new XMLOutputter(Format.getPrettyFormat());
		l_outputter.output(l_document, new FileOutputStream(p_OutputFilePath));
		long fifth = System.currentTimeMillis();
		logger.debug("Write output " + (fifth - fourth));
	}

	private static void copyfile(String srFile, String dtFile)
			throws IOException {
		copyfile(new FileInputStream(new File(srFile)), dtFile);
		
	}
	
	private static void copyfile(InputStream in, String dtFile)
			throws IOException {
		File f2 = new File(dtFile);

		// For Append the file.
		// OutputStream out = new FileOutputStream(f2,true);

		// For Overwrite the file.
		OutputStream out = new FileOutputStream(f2);

		byte[] buf = new byte[1024];
		int len;
		while ((len = in.read(buf)) > 0) {
			out.write(buf, 0, len);
		}
		in.close();
		out.close();
		System.out.println("File copied.");
	}
}
