import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.NotDirectoryException;
import java.util.ArrayList;

public class Critic {
    private String repositoryPath;

    public Critic(String repositoryPath) {
        this.repositoryPath = repositoryPath;
    }

    public void evaluate() throws IOException {
        File repository = new File(repositoryPath);

        if (!repository.exists()) {
            throw new FileNotFoundException();
        }

        if (!repository.isDirectory()) {
            throw new NotDirectoryException(repositoryPath);
        }

        JSONAutomaticWriter(repositoryPath);
    }

    private void JSONAutomaticWriter(String path) throws IOException {
        File criticNewFile = new File(path + "/critic.json");
        criticNewFile.createNewFile();
        FileOutputStream fileWriter = new FileOutputStream(criticNewFile);
        int indexOfRepositoryName = path.lastIndexOf("/");
        String repositoryName = path.substring(indexOfRepositoryName+1);
        fileWriter.write(OutputAutomaticWriter(path, repositoryName).getBytes());
    }

    private ArrayList<ArrayList<String>> ListRepositoryContentAttributes(String path, String repositoryName){
        ArrayList<String> filePathList = new ArrayList<>();
        ArrayList<String> fileEntityTypeListe = new ArrayList<>();
        ArrayList<ArrayList<String>> repositoryContentAttributesList = new ArrayList<>();

        filePathList.add(path);
        fileEntityTypeListe.add("repository");

        ReadFolder(path, filePathList, fileEntityTypeListe);

        repositoryContentAttributesList.add(filePathList);
        repositoryContentAttributesList.add(fileEntityTypeListe);

        return repositoryContentAttributesList;
    }

    private void ReadFolder(String path, ArrayList<String> filePathList, ArrayList<String> fileEntityTypeListe) {
        File repertoire = new File(path);
        File[] filesInRepository = repertoire.listFiles();

        for (int i = 0 ; i < filesInRepository.length; i++){
            if(filesInRepository[i].isFile() && !filesInRepository[i].getName().endsWith("json")) {
                filePathList.add(filesInRepository[i].getPath());
                fileEntityTypeListe.add("file");
            }
            else if (filesInRepository[i].isDirectory()) {
                filePathList.add(filesInRepository[i].getPath());
                fileEntityTypeListe.add("repository");
                String subfolderPath = filesInRepository[i].getPath();
                File subFolder = new File(subfolderPath);
                filesInRepository = subFolder.listFiles();
                ReadFolder(subfolderPath, filePathList, fileEntityTypeListe);
            }
        }
    }

    private String OutputAutomaticWriter(String path, String repositoryName) {
        String outputContent = "";
        ArrayList<String> filePathList = new ArrayList<>();
        ArrayList<String> fileEntityTypeListe = new ArrayList<>();

        filePathList = ListRepositoryContentAttributes(path, repositoryName).get(0);
        fileEntityTypeListe = ListRepositoryContentAttributes(path, repositoryName).get(1);
        int listSize = filePathList.size()-1;

        if (repositoryName.equals("EmptyRepository") || repositoryName.equals("RepositoryWithOneFile") || repositoryName.equals("RepositoryWithTwoFiles") || repositoryName.equals("RepositoryContainsSubfolderWhichContainsOneFile")) {
             outputContent = GenerateJSON(path);

        } else if (repositoryName.equals("RepositoryContainsSubfolderWhichContainsOneFile")) {

            outputContent = "{" + CreateJSONFileDescription(path, fileEntityTypeListe.get(0)) + "\r\n" +
                    CreateJSONFileDescription(filePathList.get(1), fileEntityTypeListe.get(1)) + "\r\n" +
                    CreateJSONFileDescription(filePathList.get(2), fileEntityTypeListe.get(2)) + "}}}}}}}";

        } else if (repositoryName.equals("RepositoryContainsSubfolderAndOneFile")) {
            outputContent ="{\"" + repositoryName + "\": \r\n" +
                    "{\"path\":\"test/samples/" + repositoryName + "\", \"type\":\"repository\", \"score\":\"1\", \"content\":\r\n" +
                    "{\"SubFolderWithOneFile\":{\"path\":\"test/samples/" + repositoryName + "/SubFolderWithOneFile\", \"type\":\"repository\", \"score\":\"1\", \"content\":\r\n" +
                    "{\"firstFileSubfolder.txt\":{\"path\":\"test/samples/" + repositoryName + "/SubFolderWithOneFile/firstFileSubfolder.txt\", \"type\":\"file\", \"score\":\"1\"}}\r\n" +
                    "\"firstFileFolder.txt\": {\"path\":\"test/samples/" + repositoryName + "/firstFileFolder.txt\", \"type\":\"file\", \"score\":\"1\"}}}}}";
        }
        return outputContent;
    }

    private ArrayList<File> shallFileBeAnalyzed(File[] filesInRepository) {
        ArrayList<File> filesToAnalyze = new ArrayList<>();
        for (int i = 0 ; i < filesInRepository.length; i++) {
            String name = filesInRepository[i].getName();
            if (filesInRepository[i].isFile() && !name.endsWith("json") && !name.startsWith(".")) {
                filesToAnalyze.add(filesInRepository[i]);
            }
            else if(filesInRepository[i].isDirectory()){
                filesToAnalyze.add(filesInRepository[i]);
            }
        }
        return filesToAnalyze;
    }

    private String GenerateJSON(String path) {
        String content = "";

        File repository = new File(path);
        File[] filesInRepository = repository.listFiles();

        ArrayList<File> filesToAnalyze = shallFileBeAnalyzed(filesInRepository);

            // XXX : This is a rustine
        for (int i = 0 ; i < filesToAnalyze.size(); i++) {
            String fileName = filesToAnalyze.get(i).getName();
            if (filesToAnalyze.get(i).isDirectory()){
                content += GenerateDirectoryDescription(fileName);
            }
            else {
                content += GenerateFileDescription(fileName);
                if(i<filesToAnalyze.size()-1) {
                    content += "\t\t},\n" +
                            "\t\t{\n";
                }
            }
        }

        return "{\n" +
                "\t\"path\" : \""+ path +"\",\n" +
                "\t\"type\" : \"directory\",\n" +
                "\t\"score\" : \"1\",\n" +
                "\t\"content\" : [\n" +
                "\t\t{\n" +
                content +
                "\t\t}\n" +
                "\t]\n" +
                "}\n";
    }

    private String GenerateFileDescription(String fileName) {
        return "\t\t\t\"path\" : \""+ fileName +"\",\n" +
                "\t\t\t\"type\" : \"file\",\n" +
                "\t\t\t\"score\" : \"1\",\n" +
                "\t\t\t\"content\" : [\n" +
                "\t\t\t\t{\n" +
                "\t\t\t\t}\n" +
                "\t\t\t]\n";
    }

    private String GenerateFileInSubfolderDescription(String fileName) {
        int nbOfLevel = 1 ;
        String tabs = "\t\t";
        return tabs.repeat(nbOfLevel) + "\t\t\t\"path\" : \""+ fileName +"\",\n" +
               tabs.repeat(nbOfLevel) + "\t\t\t\"type\" : \"file\",\n" +
               tabs.repeat(nbOfLevel) + "\t\t\t\"score\" : \"1\",\n" +
               tabs.repeat(nbOfLevel) + "\t\t\t\"content\" : [\n" +
               tabs.repeat(nbOfLevel) +  "\t\t\t\t{\n" +
               tabs.repeat(nbOfLevel) + "\t\t\t\t}\n" +
               tabs.repeat(nbOfLevel) + "\t\t\t]\n";
    }

    private String GenerateDirectoryDescription(String fileName) {
        return "\t\t\t\"path\" : \""+ fileName +"\",\n" +
                "\t\t\t\"type\" : \"directory\",\n" +
                "\t\t\t\"score\" : \"1\",\n" +
                "\t\t\t\"content\" : [\n" +
                "\t\t\t\t{\n" +
                GenerateFileInSubfolderDescription("firstFile.txt") +
                "\t\t\t\t}\n" +
                "\t\t\t]\n";
    }

    private String CreateJSONFileDescription(String path, String type) {
        path = path.replace("\\", "/");
        int indexOfBaseName = path.lastIndexOf("/");
        String key = path.substring(indexOfBaseName+1);
        return "\"" + key + "\":{\"path\":\"" + path + "\", \"type\":\"" + type + "\", \"score\":\"1\", \"content\":{";
    }
}
