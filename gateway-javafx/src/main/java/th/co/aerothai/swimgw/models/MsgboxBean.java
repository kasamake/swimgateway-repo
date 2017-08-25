package th.co.aerothai.swimgw.models;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;
import javax.persistence.PersistenceContext;
import th.co.aerothai.swimgw.models.Msgbox;
import th.co.aerothai.swimgw.models.Msgboxattachment;
//import th.co.aerothai.swimgw.services.impl.EntityManagerFactory;

public class MsgboxBean {
	 // @PersistenceContext(unitName="jpa-swimgw")
	// private EntityManager entityManager;
	// EntityManagerFactory entityManagerFactory =
	// Persistence.createEntityManagerFactory("jpa-swimgw");
	// Get the Entity Manager
	// EntityManager entityManager = entityManagerFactory.createEntityManager();
	@PersistenceContext
	static EntityManager em = PersistenceManager.INSTANCE.getEntityManager();

	public static Msgbox addMsgbox(Msgbox msgbox) {
		System.out.println("Add Msgbox");
		
		
		try{
			if(!em.getTransaction().isActive()){
				em.getTransaction().begin();
			}
			em.persist(msgbox);
			em.getTransaction().commit();
		} catch (Exception e) {
			e.printStackTrace();
		}

		
//		em.flush();
//		em.close();
//		PersistenceManager.INSTANCE.close();
		System.out.println("Msgbox ID: " + msgbox.getId());
		for (Msgboxattachment msgBoxAttachment : msgbox.getMsgboxattachments()) {
			saveFileAttachment(msgBoxAttachment, msgBoxAttachment.getId().intValue(), msgbox.getId());
		}

		return msgbox;
	}

	public static boolean addMsgbox(List<Msgbox> msgboxes) {
		System.out.println("Add msgboxes");
//		EntityManager em = PersistenceManager.INSTANCE.getEntityManager();
		if(!em.getTransaction().isActive()){
			em.getTransaction().begin();
		}
//		em.getTransaction().begin();

		for (int i = 0; i < msgboxes.size(); i++) {
			Msgbox msgbox = (Msgbox) msgboxes.get(i);
			em.persist(msgbox);
			System.out.println("Msgbox ID: " + msgbox.getId());
			for (Msgboxattachment msgBoxAttachment : msgbox.getMsgboxattachments()) {
				saveFileAttachment(msgBoxAttachment, msgBoxAttachment.getId().intValue(), msgbox.getId());
			}
		}
		em.getTransaction().commit();
//		em.close();
//		PersistenceManager.INSTANCE.close();
		return true;
	}

	public Msgbox getMsgbox(int id) {
		System.out.println("Get Msgbox id: " + id);
//		EntityManager em = PersistenceManager.INSTANCE.getEntityManager();
//		em.getTransaction().begin();
		Msgbox msgbox = (Msgbox) em.find(Msgbox.class, Integer.valueOf(id));
//		em.close();
//		PersistenceManager.INSTANCE.close();
		return msgbox;
	}

	public static int saveFileAttachment(Msgboxattachment msgBoxAttachment, int msgBoxAttachmentId, int msgBoxId) {
		FileOutputStream fos;
		try {
			Path directoryPath = Paths.get("SwimMsgBoxAttachment");
//			System.out.println("Directory path: " + directoryPath.toAbsolutePath().toString());
			File directory = directoryPath.toAbsolutePath().toFile();
			if (!directory.exists()) {
				directory.mkdirs();
			}

			String fileName = msgBoxId + "_" + msgBoxAttachmentId + "_" + msgBoxAttachment.getFilename();
			Path filePath = Paths.get("SwimMsgBoxAttachment", fileName);

			fos = new FileOutputStream(filePath.toAbsolutePath().toString());
			fos.write(msgBoxAttachment.getBfile());
			fos.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}

		return 0;
	}
}
