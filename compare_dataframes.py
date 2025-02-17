import pandas as pd

def compare_dataframes(df1, df2, key_column):
    """
    Compares two DataFrames based on a common column to find matching and missing rows.

    :param df1: Smaller DataFrame
    :param df2: Larger DataFrame
    :param key_column: Column name on which to compare
    :return: Two DataFrames - (matching_rows, missing_rows)
    """
    # Ensure the key column exists in both DataFrames
    if key_column not in df1.columns or key_column not in df2.columns:
        raise ValueError(f"Column '{key_column}' must exist in both DataFrames")

    # Find matching rows
    matching_rows = df1[df1[key_column].isin(df2[key_column])]

    # Find missing rows (exists in df1 but not in df2)
    missing_rows = df1[~df1[key_column].isin(df2[key_column])]

    return matching_rows, missing_rows

# Example DataFrames
data1 = {"cusip": ["12345678", "87654321", "56781234"], "value": [100, 200, 300]}
data2 = {"cusip": ["12345678", "56781234", "99999999", "44444444"], "value": [150, 350, 500, 600]}

df1 = pd.DataFrame(data1)  # Smaller set
df2 = pd.DataFrame(data2)  # Larger set

# Compare DataFrames
matching, missing = compare_dataframes(df1, df2, key_column="cusip")

# Display results
import ace_tools as tools  # For visual display
tools.display_dataframe_to_user("Matching Rows", matching)
tools.display_dataframe_to_user("Missing Rows", missing)
