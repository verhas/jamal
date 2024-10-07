# Security Policy

This document describes how Jamal is handling security through version updates, patches and security features.

## Introduction

Jamal is a versatile text-processing library developed in Java, designed to efficiently handle reading and writing of text files.
Its functionality is extended through a macro language, which is Turing complete, although this has not yet been rigorously proven.
While Jamal fosters an open development environment, this openness necessitates careful consideration of security, especially when processing text from potentially unreliable sources.

The core architecture of Jamal, including both the Java codebase and the macros that accompany the tool, has been crafted with a strong emphasis on security.
The structure of the Jamal Java code is particularly designed to support embedding in a sandboxed environment.
This is facilitated through an internal single point of Service Provider Interface (SPI), which centralizes all functionalities that access the environment.
Such a design allows for stringent control and limitation of these functionalities when Jamal is operated within a sandbox, significantly mitigating potential security risks.

## Supported Versions

### Current Support Policy

Jamal is committed to maintaining the highest level of security and functionality in its text-processing library.
To achieve this, **the latest release of Jamal is always actively supported**.

Currently, the latest version is `+2.8.1+`.

The development version is `+2.8.2-SNAPSHOT+`

Users are strongly encouraged to upgrade to the newest version to benefit from the latest features, improvements, and security patches.

### Legacy Support

While the primary focus is on the current release, we understand that upgrading to the latest version may not always be immediately feasible for all users.
In cases where a serious security issue is reported in an older version, **support and patches will be considered under specific conditions**:

- **Severity and Impact**: The security issue must be significant and pose a considerable risk to users.
- **Demand for the Patch**: There must be a reasonable and strong demand for a patch among users who are unable to upgrade to the latest version.
This is often evaluated based on the user base's feedback and the critical nature of the environments where the older version is deployed.
- **Feasibility**: The feasibility of backporting a patch to an older version will be considered.
If the changes required are too extensive or if integrating the patch disrupts the stability of the older version, users will be advised to transition to a more recent version.

### Upgrading

Users of Jamal are advised to plan regular updates to their installations to leverage the benefits of the most recent release.
By staying updated, users ensure they receive the most robust security measures, performance enhancements, and new features available in Jamal.

For assistance with upgrading or inquiries about support for older versions, please consult the official documentation or contact our support team.

## Reporting a Vulnerability

### Reporting a Vulnerability in Jamal

#### Introduction

The security and reliability of Jamal are paramount to us, and the community plays a crucial role in identifying and addressing potential security vulnerabilities.
If you discover a security issue within Jamal, we highly appreciate your contribution through responsible reporting.
This guide outlines the process for reporting security vulnerabilities.

#### Where to Report

Vulnerabilities should be reported via GitHub Issues at [Jamal GitHub Issues](https://github.com/verhas/jamal/issues).
This platform helps us manage and track issues efficiently and transparently.

#### How to Report

To ensure effective handling of your report, please include the following details in your GitHub issue:

1. **Environment Description**:
- **Java Version**: Specify the version of Java used.
- **Operating System**: Detail the operating system and version on which Jamal is running.
- **Jamal Version**: Indicate the version of Jamal where the vulnerability was found.
- **Macro Packages**: List any macro packages you were using, if applicable.

2. **Issue Description**:
- Provide a clear and concise description of what the issue is.
- Explain the potential impact of the vulnerability if it is exploited.

3. **Steps to Reproduce**:
- Include the simplest and shortest sample code or steps that demonstrate the issue.
- Ensure the reproducibility of the problem with your provided steps.

#### Handling Sensitive Information

If public disclosure of the vulnerability details on GitHub poses an increased security risk to other users, follow these modified steps:

1. **Create a GitHub Issue**:
- Include only a general description of the issue without detailed specifics that could inform an exploit.
- Mention that detailed information has been or will be provided privately.

2. **Email Detailed Information**:
- Send an email to Peter Verhas at [peter@verhas.com](mailto:peter@verhas.com) with detailed information about the vulnerability, including sensitive details that were not suitable for public posting.
- Reference the GitHub issue number in your email to link the discussion.

#### Tracking and Resolution

Once a report is submitted, the Jamal team will review the submission and work with you to understand and address the issue.
The GitHub issue will be used to track progress and discussions.
It will also record the resolution details and patch availability when resolved.

#### Best Practices for Reporting

- **Do Not Disclose Publicly**: Avoid sharing details about the vulnerability in public forums until it has been fully assessed and mitigated.
- **Provide Clear Information**: The quality and clarity of the report can significantly impact the speed and effectiveness of our response.
- **Follow Up**: Stay engaged with the issue thread to provide additional information and clarification as needed.

By following these guidelines, you help maintain the security and integrity of Jamal, benefiting the entire user community.
Thank you for your support and cooperation in keeping Jamal secure.

## Security Considerations for Jamal

### Possible Attack Vectors

Although there are no known vulnerabilities within Jamal at the time of writing, we recognize the theoretical possibilities of attack vectors arising, particularly due to its openness and extensibility features.
Here are some potential attack vectors:

1. **Macro Exploits**: Since the macro language is extensible and Turing complete, malicious macros could be crafted to perform undesirable actions.
These could range from unauthorized data manipulation to execution of arbitrary code, if not properly sandboxed or restricted.

2. **Input File Manipulation**: Handling files from unreliable sources without adequate validation can lead to security breaches.
Attackers could craft files designed to exploit specific vulnerabilities in the parsing and processing logic of Jamal.

3. **Code Injection**: There is a theoretical risk of code injection, where malicious code or scripts are embedded in text files that Jamal processes, potentially leading to execution of the injected code.

4. **Denial of Service (DoS)**: By crafting input that leads to excessive computation, an attacker might attempt to render a service unresponsive, exploiting the Turing completeness of the macro system to create loops or intensive computations.

### Mitigation Strategies

To safeguard against potential security threats, the following mitigation strategies are recommended:

* **Sandboxing**: Implementing sandboxing strategies for Jamal can significantly enhance security by limiting the library's operational scope and preventing potentially malicious code from accessing critical system resources and sensitive data.
Here are effective sandboxing approaches suitable for users and administrators:

  * **Custom Security Policies**: Administrators can enforce custom security policies at runtime that strictly define permissions for file access, network connections, and system commands.
By using Java's built-in capabilities to enforce these policies, administrators ensure that Jamal operates within predefined security constraints.

  * **Containerization**: Deploying Jamal within containers, such as Docker, provides a controlled and isolated environment.
This method restricts Jamal's access to the host system's resources, minimizing the potential impact of any security breaches and facilitating secure instance management and deployment.

  * **Virtual Machines**: Operating Jamal within a virtual machine offers a strong layer of isolation between the Jamal process and the host operating system.
This setup helps contain any potential security issues within the virtual environment, thus protecting the host system from direct threats.

  * **Restricted API Access**: Restricting API access by configuring Jamal to minimize its exposure to potentially harmful system operations can effectively reduce its attack surface.
This involves configuring the environment to limit Jamal's capabilities to essential functions only, based on the security requirements of the deployment context.

These sandboxing techniques empower administrators to tightly control Jamal's execution environment, mitigating the risk of exploitation while preserving essential functionalities.

* **Input Validation**: Implement strict validation rules for input files, especially those sourced from untrusted origins, to prevent malformed data from triggering unexpected behavior.

* **Limit Macro Capabilities**: Macros significantly enhance Jamal's functionality.
However, software developers who write Java or Kotlin code for these macros have the ability to limit their capabilities.
By designing macros with restricted functionalities or providing mechanisms for administrators to disable or restrict macro execution in high-risk scenarios, developers can greatly diminish the potential for exploits.
This approach of constraining macro capabilities has also been implemented in the development of the macros included with the Jamal library.

- **Regular Audits and Updates**: Continual security audits and prompt updates to the Jamal library as vulnerabilities are discovered or theoretical exploits become practical concerns are crucial for maintaining security integrity over time.

### Conclusion

The Jamal project takes security seriously, incorporating robust design principles and thoughtful architecture to minimize risk.
While the open and extensible nature of Jamal introduces potential vulnerabilities, adherence to recommended security practices and configurations can greatly mitigate these risks.
As with any software, maintaining vigilance through monitoring, updating, and educating users about secure usage practices remains paramount.