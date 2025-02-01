import io
import sys

#
# This is the python code copied by Jamal to a temporary file and started using an external python interpreter.
# It receives python code on the standard input, executes and sends the result to the standard output.
#

def calculate_delimiter(code):
    """
    calculates and returns a delimiter string guaranteed not to be any of the lines of the code
    :param code: is the string that needs a delimiter
    :return: the delimiter string
    """
    lines = code.split("\n")
    delim = ""

    for l in lines:
        if delim == l:
            delim += '.'

        if len(l) > len(delim):
            delim += 'B' if l[len(delim)] == 'A' else 'A'

    return delim


#
# The main loop of the processor, reading the standard input, processing the Python code received on the input
# executing the code. The result of the execution is the standard output.
#
# If there is any exception during the execution then the exception is printed to the standard output.
#
# The input is
#           delimiter line
#           python code
#           delimiter line
#
# assuming that the "delimiter line" is none of the lines of the python code.
#
# The standard output is redirected and collected and then printed to the real standard output in a similarly
# delimited fashion.
#
while True:
    try:
        delimiter = sys.stdin.readline()[:-1]
        if not delimiter:
            continue

        code_lines = []
        while True:
            line = sys.stdin.readline()[:-1]
            if line == delimiter:
                break
            code_lines.append(line)

        code_to_execute = "\n".join(code_lines)
        buffer = io.StringIO()
        sys.stdout = buffer
        try:
            exec(code_to_execute)
        except Exception as e:
            print(e, end='')
        sys.stdout = sys.__stdout__
        result = buffer.getvalue()
        delimiter = calculate_delimiter(result)
        print(delimiter)
        print(result)
        print(delimiter)
        sys.stdout.flush()

    except EOFError:
        print("\nEnd of input detected. Exiting.")
        break

    except Exception as e:
        print(f"Error during execution: {e}")
