import jade.core.Agent;
import jade.core.behaviours.CyclicBehaviour;
import jade.lang.acl.ACLMessage;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Random;

/**
 * Classe représentant un robot dans l'atelier.
 * 
 * Un robot est caractérisé par :
 * - Une liste de compétences qu'il maîtrise avec un degré de probabilité.
 * - La capacité de recevoir des tâches, d'appliquer des compétences ou de déléguer une tâche.
 * - La gestion des compétences en collaboration avec d'autres robots.
 * 
 * Fonctionnalités principales :
 * - Exécuter les tâches assignées par l'Atelier.
 * - Déléguer des tâches à d'autres robots en cas d'échec.
 * - Aider d'autres robots si disponible.
 * - Envoyer des confirmations de tâches terminées à l'Atelier.
 * 
 */
public class RobotAgent extends Agent {
    private HashMap<String, Double> skills = new HashMap<>();
    private HashSet<String> handledTasks = new HashSet<>();
    private String currentTask = null;
    private int messageCount = 0;

    /**
     * Initialisation du robot.
     * Configure ses compétences dynamiquement en fonction des arguments reçus.
     */
    @Override
    protected void setup() {
        Object[] args = getArguments();
        int totalSkills = args != null && args.length > 0 ? (int) args[0] : 3;
        System.out.println(getLocalName() + " initialisé avec " + totalSkills + " compétences.");

        generateSkills(totalSkills);

        ACLMessage msg = new ACLMessage(ACLMessage.INFORM);
        msg.addReceiver(getAID("Atelier"));
        msg.setContent("Compétences disponibles : " + skills.toString() + ", Messages envoyés : " + messageCount);
        send(msg);
        messageCount++;

        addBehaviour(new CyclicBehaviour() {
            @Override
            public void action() {
                ACLMessage msg = receive();
                if (msg != null) {
                    handleMessage(msg);
                } else {
                    block();
                }
            }
        });
    }

    /**
     * Génère les compétences du robot dynamiquement.
     * 
     * @param totalSkills Le nombre total de compétences à générer.
     */
    private void generateSkills(int totalSkills) {
        Random random = new Random();
        for (int i = 1; i <= totalSkills; i++) {
            String skillName = "skill" + i;
            skills.put(skillName, 0.5 + (0.5 * random.nextDouble())); // Probabilité entre 0.5 et 1.0
        }
    }

    /**
     * Traite un message reçu par le robot.
     * Peut être un message de tâche assignée ou une demande d'aide d'un autre robot.
     * 
     * @param msg Le message reçu.
     */
    private void handleMessage(ACLMessage msg) {
        if (msg.getPerformative() == ACLMessage.REQUEST) {
            // Tâche assignée par l'Atelier
            System.out.println(getLocalName() + " a reçu une tâche : " + msg.getContent());
            if (currentTask == null) {
                currentTask = msg.getContent();
                handledTasks.add(currentTask);
                executeTask(currentTask);
            } else {
                System.out.println(getLocalName() + " est déjà occupé.");
            }
        } else if (msg.getPerformative() == ACLMessage.PROPOSE) {
            // Demande d'aide d'un autre robot
            handleHelpRequest(msg);
        }
    }

    /**
     * Exécute une tâche assignée au robot.
     * Applique les compétences au produit ou délègue la tâche en cas d'échec.
     * 
     * @param task La tâche reçue.
     */
    private void executeTask(String task) {
        Product product = Product.deserialize(task);
    
        for (String skill : product.getRequiredSkills()) {
            if (skills.containsKey(skill) && Math.random() < skills.get(skill)) {
                System.out.println(getLocalName() + " réussit à appliquer la compétence " + skill);
                product.applySkill(skill); // Mettre la compétence à "true"
            } else {
                System.out.println(getLocalName() + " échoue à appliquer la compétence " + skill);
                delegateTask(skill, product);
                return; // Arrête le traitement pour déléguer
            }
        }
    
        // Si le produit est complet, envoyer un message CONFIRM à l'atelier
        if (product.isComplete()) {
            System.out.println("Produit " + product.getName() + " est entièrement traité par " + getLocalName());
            ACLMessage confirmMsg = new ACLMessage(ACLMessage.CONFIRM);
            confirmMsg.addReceiver(getAID("Atelier"));
            confirmMsg.setContent(product.getName()); // Envoi du nom du produit terminé
            send(confirmMsg);
            currentTask = null; // Libérer le robot
        }
    }    

    /**
     * Délègue une compétence à un autre robot en cas d'échec.
     * Envoie un message PROPOSE à tous les autres robots.
     * 
     * @param skill La compétence échouée.
     * @param product L'état actuel du produit.
     */
    private void delegateTask(String skill, Product product) {
        System.out.println(getLocalName() + " cherche un robot pour déléguer la tâche : " + skill);

        ACLMessage helpRequest = new ACLMessage(ACLMessage.PROPOSE);
        helpRequest.setContent(skill + "," + product.serialize());
        helpRequest.setConversationId("help-request");
        for (int i = 1; i <= 5; i++) { // Supposons un maximum de 5 robots
            if (!getLocalName().equals("Robot" + i)) {
                helpRequest.addReceiver(getAID("Robot" + i));
            }
        }
        send(helpRequest);
    }

    /**
     * Gère une demande d'aide reçue d'un autre robot.
     * Applique la compétence demandée si possible.
     * 
     * @param msg Le message contenant la demande d'aide.
     */
    private void handleHelpRequest(ACLMessage msg) {
        if (currentTask != null) {
            System.out.println(getLocalName() + " est occupé et ne peut pas aider.");
            return;
        }
    
        String[] content = msg.getContent().split(",", 2);
        String skill = content[0];
        Product product = Product.deserialize(content[1]);
    
        if (skills.containsKey(skill) && Math.random() < skills.get(skill)) {
            System.out.println(getLocalName() + " accepte d'aider pour la compétence : " + skill);
            product.applySkill(skill); // Mettre à jour la compétence dans le produit
    
            // Vérifier si le produit est complet après l'aide
            if (product.isComplete()) {
                System.out.println("Produit " + product.getName() + " est entièrement traité après l'aide de " + getLocalName());
                ACLMessage confirmMsg = new ACLMessage(ACLMessage.CONFIRM);
                confirmMsg.addReceiver(getAID("Atelier"));
                confirmMsg.setContent(product.getName());
                send(confirmMsg);
            }
        } else {
            System.out.println(getLocalName() + " ne peut pas aider pour " + skill);
        }
    }
    
}
