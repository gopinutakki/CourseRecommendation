/**
 * Course Recommendation System. Western Kentucky University.
 */
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

public class CourseEdges {

	static CourseList crsList = new CourseList();
	static UserList usrList = new UserList();
	static Edges edgeList = new Edges();
	static NodeCentrality centrality = new NodeCentrality();

	/**
	 * @param args
	 * @throws IOException
	 */
	public static void main(String[] args) throws IOException {
		BufferedReader userList = new BufferedReader(
				new FileReader("input.txt"));
		BufferedReader coursesList = new BufferedReader(new FileReader(
				"courses.txt"));
		String line = "";

		while ((line = coursesList.readLine()) != null) {
			String course[] = line.split(",");
			crsList.list
					.add(new Course(course[0], Integer.parseInt(course[1])));
		}

		while ((line = userList.readLine()) != null) {
			String userDetails[] = line.split(",");
			usrList.add(userDetails[0], userDetails[1]);
		}
		usrList.computeEdges();
		edgeList.print();

		centrality.extractRecommendations("centralityOutput.txt",
				"recommendations.txt");
		System.out.println("Done!");
	}
}

class UserList {

	ArrayList<User> users = new ArrayList<User>();

	public void add(String ipAdd, String crsName) {
		int crsID = CourseEdges.crsList.getCrsID(crsName);
		for (int looper = 0; looper < users.size(); looper++) {
			if (users.get(looper).ip.equals(ipAdd)) {
				if (crsID >= 0) {
					users.get(looper).addCourse(crsID);
					return;
				}
			}
		}
		users.add(new User(ipAdd, crsID));
	}

	public void computeEdges() throws IOException {
		for (int index = 0; index < users.size(); index++) {
			int limit = users.get(index).list.size() / 2;
			int limit2 = users.get(index).list.size();
			for (int crs = 0; crs < limit; crs++) {
				for (int crs2 = limit + 1; crs2 < limit2; crs2++) {
					CourseEdges.edgeList.addEdge(
							users.get(index).list.get(crs),
							users.get(index).list.get(crs2));
				}
			}
		}
	}
}

class User {

	String ip = "";
	ArrayList<Integer> list = new ArrayList<Integer>();

	public User(String ipAdd) {
		this.ip = ipAdd;
	}

	public User(String ipAdd, int course) {
		this.ip = ipAdd;
		this.list.add(course);
	}

	public void addCourse(int course) {
		for (int index = 0; index < this.list.size(); index++) {
			if (this.list.get(index) == course) {
				return;
			}
		}
		this.list.add(course);
	}
}

class CourseList {

	ArrayList<Course> list = new ArrayList<Course>();

	public void add(Course crs) {
		list.add(crs);
	}

	public int getCrsID(String crsName) {
		for (int index = 0; index < list.size(); index++) {
			if (list.get(index).name.equals(crsName)) {
				return list.get(index).id;
			}
		}
		return -1;
	}

	public String getCrsName(int id) {
		for (int index = 0; index < list.size(); index++) {
			if (list.get(index).id == id) {
				return list.get(index).name;
			}
		}
		return null;
	}
}

class Course {

	String name = "";
	int id;

	public Course(String crsName, int crsID) {
		this.name = crsName;
		this.id = crsID;
	}
}

class Edges {

	ArrayList<Edge> edges = new ArrayList<Edge>();

	public void print() throws IOException {
		BufferedWriter edgesWriter = new BufferedWriter(new FileWriter(
				"edges.txt"));
		for (int index = 0; index < edges.size(); index++) {
			edgesWriter.write(edges.get(index).from + " " + edges.get(index).to
					+ "\n");
		}
		edgesWriter.flush();
		edgesWriter.close();
	}

	public void addEdge(int f, int t) {
		if (!exists(f, t)) {
			edges.add(new Edge(f, t));
		}
	}

	public boolean exists(int f, int t) {
		for (int index = 0; index < edges.size(); index++) {
			if ((edges.get(index).from == f && edges.get(index).to == t)) // ||
																			// (edges.get(index).from
																			// ==
																			// t
																			// &&
																			// edges.get(index).to
																			// ==
																			// f))
			{
				return true;
			}
		}
		return false;
	}
}

class Edge {

	int from, to;

	public Edge(int f, int t) {
		this.from = f;
		this.to = t;
	}
}

class NodeCentrality {

	ArrayList<Node> nodes = new ArrayList<Node>();

	public void add(String line) {
		String[] details = line.split("\t");
		nodes.add(new Node(Integer.parseInt(details[0]), Double
				.parseDouble(details[1]), Double.parseDouble(details[2]),
				Double.parseDouble(details[3]), Double.parseDouble(details[4]),
				Double.parseDouble(details[5]), Double.parseDouble(details[6]),
				Double.parseDouble(details[7]), Double.parseDouble(details[8]),
				Double.parseDouble(details[9])));
	}

	public void readResults(String file) throws IOException {
		BufferedReader results = new BufferedReader(new FileReader(file));
		String line = "";
		while ((line = results.readLine()) != null) {
			if (line.startsWith("#")) {
				continue;
			}
			add(line);
		}
	}

	public void sort() {
		Collections.sort(nodes, new Comparator() {

			@Override
			public int compare(Object arg0, Object arg1) {
				Node n1 = (Node) arg0;
				Node n2 = (Node) arg1;

				if (n1.Closeness > n2.Closeness) {
					return 1;
				} else if (n1.Closeness < n2.Closeness) {
					return -1;
				}
				return 0;
			}
		});
	}

	public void print(String out) throws IOException {
		BufferedWriter writer = new BufferedWriter(new FileWriter(out));
		for (int index = 0; index < nodes.size(); index++) {
			writer.write(CourseEdges.crsList.getCrsName(nodes.get(index).nodeID)
					+ "\n");
		}
		writer.flush();
		writer.close();
	}

	public void runCentrality(String file) throws IOException {
		Runtime.getRuntime().exec("./centrality -i:edges.txt -o:" + file);
	}

	public void extractRecommendations(String file, String out)
			throws IOException {
		runCentrality(file);
		readResults(file);
		sort();
		print(out);
	}
}

class Node {

	int nodeID;
	double Degree;
	double Closeness;
	double Betweennes;
	double EigenVector;
	double NetworkConstraint;
	double ClusteringCoefficient;
	double PageRank;
	double HubScore;
	double AuthorityScore;

	public Node(int id, double deg, double cl, double be, double ev, double nc,
			double cc, double pr, double hs, double as) {

		this.nodeID = id;
		this.Degree = deg;
		this.Closeness = cl;
		this.Betweennes = be;
		this.EigenVector = ev;
		this.NetworkConstraint = nc;
		this.ClusteringCoefficient = cc;
		this.PageRank = pr;
		this.HubScore = hs;
		this.AuthorityScore = as;
	}
}
