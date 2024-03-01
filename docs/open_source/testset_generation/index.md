# 🧰 RAG Testset Generation

> ⚠️ **The RAG toolset is currently in early version and is subject to change**. Feel free to reach out on our [Discord server](https://discord.gg/fkv7CAr3FE) if you have any trouble with test set generation or to provide feedback.


The Giskard python library provides a toolset dedicated to Retrieval Augmented Generative models (RAGs) that generates a list of `question`, `reference_answer` and `reference_context` from the knowledge base of the model. The generated test set is then used to evaluate your RAG model. All questions are asked to your model and its answers are compared against the reference answers to create a score. 

(difficulty_levels)=
## Generate different questions types

You can currently generate multiple questions type. Each type of question helps you to evaluate specific componenents of your RAG. More question types will be added in the future to provide thorough evaluation of RAG systems. 

```{list-table}
:header-rows: 1
:widths: 28, 42, 30
* - Question type
  - Description
  - Targeted RAG component
* - **1: Simple questions**
  - Simple questions generated from an excerpt of the knowledge base
  - Basic retrieval and LLM generation
* - **2: Complex questions**
  - Questions made more complex by paraphrasing
  - LLM understanding and generation 
* - **3: Distracting questions**
  - Questions made to confuse the retrieval part of the RAG with a distracting element from the knowledge base but irrelevant to the question
  - Robustness of retrieval
* - **4: Situational questions**
  - Questions including user context to evaluate the ability of the generation to produce relevant answer according to the context
  - LLM understanding and generation
* - **5: Double questions**
  - Questions with two distinct parts to evaluate the capabilities of the query rewriter of the RAG 
  - RAG rewriter
* - **6: Conversational questions**
  - Questions made as part of a conversation, first message describe the context of the question that is ask in the last message, also tests the rewriter
  - RAG rewriter
```

## Before starting

Before starting, make sure you have installed the LLM flavor of Giskard:

```bash
pip install "giskard[llm]"
```

To use the RAG test set generation and evaluation tools, you need to have an OpenAI API key. You can set it in your notebook
like this:

:::::::{tab-set}
::::::{tab-item} OpenAI

```python
import os

os.environ["OPENAI_API_KEY"] = "sk-…"
```

::::::
::::::{tab-item} Azure OpenAI

Require `openai>=1.0.0`
Make sure that both the LLM and Embeddings models are both deployed on the Azure endpoint. The default embedding model used by the Giskard client is `text-embedding-ada-002`. 

```python
import os
from giskard.llm import set_llm_model

os.environ['AZURE_OPENAI_API_KEY'] = '...'
os.environ['AZURE_OPENAI_ENDPOINT'] = 'https://xxx.openai.azure.com'
os.environ['OPENAI_API_VERSION'] = '2023-07-01-preview'


# You'll need to provide the name of the model that you've deployed
# Beware, the model provided must be capable of using function calls
set_llm_model('my-gpt-4-model')
```

::::::
:::::::

We are now ready to start.


## Step 1: Automatically generate a Q&A test set

To start, you only need your data or knowledge base in a pandas `DataFrame`. Then, you can initialize the {class}`~giskard.rag.knowledge_base.KnowledgeBase` by passing your dataframe. 

If some columns in your dataframe are not relevant for the generation of questions (e.g. they contain metadata), make sure you specify
column names to the `knowledge_base_columns` argument (see {class}`giskard.rag.knowledge_base.KnowledgeBase`).

To make the question generation more accurate, you can also provide a description of your assistant. This will help the generator to generate questions that are more relevant to your assistant's task. 


```python

from giskard.rag import generate_testset, KnowledgeBase

# Load your data and initialize the KnowledgeBase
knowledge_base_df = pd.read_csv("path/to/your/knowledge_base.csv")
knowledge_base = KnowledgeBase(knowledge_base_df, 
                               knowledge_base_columns=["column_1", "column_2"])
```

We are ready to generate the test set. We can start with a small test set of 10 questions and answers for each question type.

Currently, you can choose the question types from 1 to 6 (see {ref}`difficulty_levels`)

```python
# Generate a testset with 10 questions & answers for each question types (this will take a while)
testset = generate_testset(
    knowledge_base, 
    num_questions=10,
    question_types=[1, 2]
    language='en', # Optional, if you want to  generate questions in a specific language
    # Optionally, you can provide a description of your RAG assistant to improve the questions quality.
    assistant_description="An assistant that answers common questions about our products",
)

# Save the generated testset
testset.save("my_testset.jsonl")

# Load it back
from giskard.rag import QATestset

loaded_testset = QATestset.load("my_testset.jsonl")
```

The test set will be an instance of {class}`~giskard.rag.QATestset`. You can save it and load it later with `QATestset.load("path/to/testset.jsonl")`.

You can also convert it to a pandas DataFrame with `testset.to_pandas()`:

```py
# Convert it to a pandas dataframe
df = loaded_testset.to_pandas()
```

Let's have a look at the generated questions:

| question | reference_context | reference_answer |  metadata |
|----------|-------------------|------------------|-----------|
| For which countries can I track my shipping? | What is your shipping policy? We offer free shipping on all orders over \$50. For orders below \$50, we charge a flat rate of \$5.99. We offer shipping services to customers residing in all 50 states of the US, in addition to providing delivery options to Canada and Mexico. ------  How can I track my order? Once your purchase has been successfully confirmed and shipped, you will receive a confirmation email containing your tracking number. You can simply click on the link provided in the email or visit our website's order tracking page. | We ship to all 50 states in the US, as well as to Canada and Mexico. We offer tracking for all our shippings. | {"question_type": 1} |

As you can see, the data contains 4 columns:
- `question`: the generated question
- `reference_context`: the context that can be used to answer the question
- `reference_answer`: the answer to the question (generated with GPT-4)
- `conversation_history`: not shown in the table above, contain the history of the conversation with the assistant as a list, only relevant for conversational question, otherwise it contains an empty list.
- `metadata`: a dictionnary with various metadata about the question, this includes the `question_type` (an integer between 1 and 6)

## Step 2: Evaluate your model on the generated test set

Before evaluating your model, you must wrap it as a `giskard.Model`. This step is necessary to ensure a common format for your model and its metadata. You can wrap anything as long as you can represent it in a Python function (for example an API call to Azure or OpenAI). We also have pre-built wrappers for LangChain objects, or you can create your own wrapper by extending the `giskard.Model` class if you need to wrap a complex object such as a custom-made RAG communicating with a vectorstore.

To do so, you can follow the instructions from the [LLM Scan feature](../scan/scan_llm/index.md#step-1-wrap-your-model). Make sure that you pass `feature_names = "question"` when wrapping your model, so that it matches the question column of the test set.

Detailed examples can also be found on our {doc}`LLM tutorials section </tutorials/llm_tutorials/index>`.

Once you have wrapped your model, we can proceed with evaluation.

Let's convert our test set into an actionable test suite ({class}`giskard.Suite`) that we can save and reuse in further iterations.

```python
test_suite = testset.to_test_suite("My first test suite")

test_suite.run(model=giskard_model)
```

![](./test_suite_widget.png)


Note that you can split the test suite on the question metadata values, for instance on each question type. 

```python
test_suite_by_question_types = testset.to_test_suite("Split test suite", slicing_metadata=["question_type"])
test_suite.run(model=giskard_model)
```

Jump to the [test customization](https://docs.giskard.ai/en/latest/open_source/customize_tests/index.html) and [test integration](https://docs.giskard.ai/en/latest/open_source/integrate_tests/index.html) sections to find out everything you can do with test suites.


## Step 3: upload your test suite to the Giskard Hub

Uploading a test suite to the hub allows you to:
* Compare the quality of different models and prompts to decide which one to promote
* Create more tests relevant to your use case, combining input prompts that make your model fail and custome evaluation criteria
* Share results, and collaborate with your team to integrate business feedback

To upload your test suite, you must have created a project on Giskard Hub and instantiated a Giskard Python client. If you haven't done this yet, follow the first steps of [upload your object](https://docs.giskard.ai/en/latest/giskard_hub/upload/index.html#upload-your-object) guide. 

Then, upload your test suite and model like this:
```python
test_suite.upload(giskard_client, project_id)  # project_id should be the id of the Giskard project in which you want to upload your suite
giskard_model.upload(giskard_client, project_id)
```

> ⚠️ To upload your model to the hub, it must be pickleable. If your model is not, you must extend the `giskard.Model` class and override the `save_model` and `load_model` methods to properly save and load the non-pickleable parts of your model (e.g. the vector store). You can find an [example here](../scan/scan_llm/index.md#step-1-wrap-your-model) inside the "Wrap a custom RAG" tab.

[Here's a demo](https://huggingface.co/spaces/giskardai/giskard) of the Giskard Hub in action.


## What data are being sent to OpenAI/Azure OpenAI

In order to perform the question generation, we will be sending the following information to OpenAI/Azure OpenAI:

- Data provided in your knowledge base
- Text generated by your model
- Model name and description

## Will the test set generation work in any language?
Yes, you can specify the language of the generated questions when you initialize the {class}`giskard.rag.TestsetGenerator`.

## Troubleshooting
If you encounter any issues, join our [Discord community](https://discord.gg/fkv7CAr3FE) and ask questions in our #support channel.